#!/usr/bin/env python3
"""Capture identical-game-state Storybook vs Overhead Woven Trail QA screenshots.

Runs one immutable, independently signed and built Android QA APK. It never
changes saves, config, APK identity or the game's rules. An emulator image
is comparison evidence, not physical owner-device art approval.
"""
import hashlib
import json
import os
from pathlib import Path
import re
import struct
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
PKG = "com.playereden.gridmates.crossroads.qa"
ACTIVITY = PKG + "/com.playereden.gridmates.crossroads.MainActivity"
CODE = int(os.environ.get("QA_CODE", "1672"))
SHA = os.environ.get("QA_SHA", "f4a27d23a7ac9ba9a8efef6b5c1fdb441c7b236bf2200398ebe95edac44ffeba")
APK_DIR = ROOT / "qa-archives/current"
APK = APK_DIR / f"GridMates-Crossroads-QA-{CODE}.apk"
EVIDENCE = ROOT / "qa-evidence"
EVIDENCE.mkdir(exist_ok=True)


def call(*args, timeout=120):
    result = subprocess.run(args, text=True, capture_output=True,
                            check=True, timeout=timeout)
    return result.stdout


def adb(*args, timeout=120):
    return call("adb", *args, timeout=timeout)


def sha256(path):
    h = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


receipt = json.loads((APK_DIR / "GridMates-Crossroads-QA-receipt.json").read_text())
assert APK.exists() and sha256(APK) == SHA
assert receipt["sha256"] == SHA
assert receipt["version_code"] == CODE
assert receipt["application_id"] == PKG
assert receipt["channel"] == "public-test-only-qa"
assert receipt["signer_cert_sha256"] == (
    "75cc3c896247d71fbd51f7a3ed41f90c8218a8a5a70df8c0f6b494b5cc23d419"
)


def tree():
    adb("shell", "uiautomator", "dump", "/sdcard/crossroads-window.xml")
    xml = adb("shell", "cat", "/sdcard/crossroads-window.xml")
    return ET.fromstring(xml)


def label_of(node):
    return (node.attrib.get("text", "") + " " +
            node.attrib.get("content-desc", "")).strip()


def touch_bounds(node):
    match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                         node.attrib.get("bounds", ""))
    if not match:
        return None
    x1, y1, x2, y2 = map(int, match.groups())
    if x2 <= x1 or y2 <= y1:
        return None
    return (str((x1 + x2) // 2), str((y1 + y2) // 2))


def tap_required(label):
    for _ in range(8):
        ui = tree()
        # Dismiss only the system's harmless first-use immersive-mode hint,
        # never a crash or ANR, and never a gameplay confirmation.
        for node in ui.iter("node"):
            if (node.attrib.get("resource-id") == "android:id/ok" and
                    node.attrib.get("text", "").strip().lower() == "got it" and
                    node.attrib.get("package") == "android"):
                coords = touch_bounds(node)
                assert coords
                adb("shell", "input", "tap", *coords)
                time.sleep(1)
                break
        else:
            candidates = [
                n for n in ui.iter("node")
                if label.lower() in label_of(n).lower() and touch_bounds(n)
            ]
            if candidates:
                coords = touch_bounds(candidates[-1])
                adb("shell", "input", "tap", *coords)
                time.sleep(2)
                print("Tapped:", label)
                return
        time.sleep(1)
    texts = [label_of(n) for n in tree().iter("node") if label_of(n)]
    raise AssertionError(f"Can't find Android control {label!r}: {texts[-35:]}")


def visible(label):
    return any(label.lower() in label_of(n).lower() for n in tree().iter("node"))


def checkpoint_hash():
    raw = adb("shell", "run-as", PKG, "cat",
              "shared_prefs/crossroads_qa_profile_v1.xml")
    values = {
        n.attrib["name"]: n.text if n.tag == "string" else n.attrib.get("value")
        for n in ET.fromstring(raw) if "name" in n.attrib
    }
    checkpoint = values.get("active_match_checkpoint_v1")
    assert checkpoint, "Fresh installed game didn't create a real match"
    state = json.loads(checkpoint)
    assert state.get("formatVersion") == 1
    assert "woodland_woven_trail" in state["boardId"]
    return hashlib.sha256(checkpoint.encode()).hexdigest()


def capture(name):
    dest = EVIDENCE / f"{name}.png"
    with dest.open("wb") as out:
        subprocess.run(["adb", "exec-out", "screencap", "-p"],
                       stdout=out, check=True, timeout=90)
    data = dest.read_bytes()
    assert len(data) > 40000, f"Tiny/blank screenshot: {dest}"
    assert data[:8] == b"\x89PNG\r\n\x1a\n"
    width, height = struct.unpack(">II", data[16:24])
    assert width >= 320 and height >= 500, (width, height)
    print("Capture", name, width, height, len(data), sha256(dest))
    return {"path": dest.name, "sha256": sha256(dest),
            "width": width, "height": height, "bytes": len(data)}


assert "Success" in adb("install", str(APK))
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(3)
tap_required("START RACE")
time.sleep(3)
assert visible("Woven Trail"), "The initial QA game must be Classic Woven Trail"
saved = checkpoint_hash()
screens = {}
screens["storybook_live"] = capture("01_storybook_live")
tap_required("Full map")
screens["storybook_full_map"] = capture("02_storybook_full_map")
tap_required("BACK TO THE TRAIL")
tap_required("Match options")
assert visible("Woven board art: Storybook (QA)"), (
    "Expected original storybook image as default"
)
tap_required("Woven board art: Storybook (QA)")
assert saved == checkpoint_hash(), "Appearance toggle modified the game checkpoint"
screens["overhead_live"] = capture("03_overhead_live")
tap_required("Full map")
screens["overhead_full_map"] = capture("04_overhead_full_map")
tap_required("BACK TO THE TRAIL")
tap_required("Match options")
assert visible("Woven board art: Overhead (QA)"), (
    "Overhead setting didn't persist in the Android UI"
)
adb("shell", "input", "keyevent", "KEYCODE_BACK")
assert saved == checkpoint_hash(), "Art selection changed the saved race"
assert (screens["storybook_live"]["sha256"] !=
        screens["overhead_live"]["sha256"]), "Both art mode screenshots are identical"
assert (screens["storybook_full_map"]["sha256"] !=
        screens["overhead_full_map"]["sha256"]), "Both full map images are identical"
(EVIDENCE / "visual85_same_race_comparison.json").write_text(
    json.dumps({
        "status": "CAPTURED_FOR_OWNER_REVIEW",
        "apk_version": CODE,
        "artifact_source_commit": receipt["source_commit"],
        "apk_sha256": SHA,
        "test_signer": receipt["signer_cert_sha256"],
        "same_legal_race_checkpoint": True,
        "checkpoint_sha256": saved,
        "captured_modes": ["storybook", "overhead"],
        "screenshot_evidence": screens,
        "real_device_accepted": False,
        "human_art_approved": False,
    }, indent=2) + "\n"
)
print("VISUAL-85 PASS: identical offline Woven save in both rendered art modes")
