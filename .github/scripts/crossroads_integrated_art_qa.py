#!/usr/bin/env python3
"""Compare the actual QA1677 and QA1678 APKs on one preserved Woven race."""
import hashlib
import json
import os
from pathlib import Path
import re
import struct
import subprocess
import time
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[2]
PKG = "com.playereden.gridmates.crossroads.qa"
ACTIVITY = PKG + "/com.playereden.gridmates.crossroads.MainActivity"
EVIDENCE = ROOT / "qa-evidence"
EVIDENCE.mkdir(exist_ok=True)
SIGNER = "75cc3c896247d71fbd51f7a3ed41f90c8218a8a5a70df8c0f6b494b5cc23d419"
ATLAS = "assets/art/v1/environment_atlas_v1.png"
ATLAS_SHA = "19819a342ecaf9249500406ded7d9ebcda3d101874168e8ec777846985cc8a49"
BUILDS = [
    ("base", 1677, "caf832d4a57ad5489437935aac6d1de3fe6bc923"),
    ("next", 1678, "6f173a4afc9c2adafee2475f35ff0522efcdbbb1"),
]

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



def verify_build(folder, code, source):
    path = ROOT / "qa-archives" / folder / f"GridMates-Crossroads-QA-{code}.apk"
    receipt = json.loads((path.parent / "GridMates-Crossroads-QA-receipt.json").read_text())
    assert receipt["sha256"] == sha256(path)
    assert receipt["version_code"] == code
    assert receipt["application_id"] == PKG
    assert receipt["channel"] == "public-test-only-qa"
    assert receipt["source_commit"] == source, receipt["source_commit"]
    assert receipt["signer_cert_sha256"] == SIGNER
    sdk = Path(os.environ.get("ANDROID_HOME") or os.environ["ANDROID_SDK_ROOT"])
    signer = sdk / "build-tools/35.0.0/apksigner"
    verified = call(str(signer), "verify", "--print-certs", str(path))
    assert SIGNER in verified.lower(), verified
    with zipfile.ZipFile(path) as archive:
        if code == 1677:
            assert ATLAS not in archive.namelist()
        else:
            data = archive.read(ATLAS)
            assert hashlib.sha256(data).hexdigest() == ATLAS_SHA
            assert data[:8] == b"\x89PNG\r\n\x1a\n"
            assert struct.unpack(">II", data[16:24]) == (1024, 1024)
            assert data[24:26] == bytes([8, 6]), "Expected 8-bit RGBA"
    return path, receipt


def launch():
    adb("shell", "am", "start", "-W", "-n", ACTIVITY)
    time.sleep(4)


def select_overhead():
    tap_required("Match options")
    if visible("Woven board art: Storybook (QA)"):
        tap_required("Woven board art: Storybook (QA)")
    else:
        assert visible("Woven board art: Overhead (QA)")
        adb("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(2)


def capture_pair(prefix, screens):
    tap_required("Full map")
    time.sleep(2)
    screens[prefix + "_full_map"] = capture(prefix + "_full_map")
    tap_required("BACK TO THE TRAIL")
    time.sleep(2)
    screens[prefix + "_live"] = capture(prefix + "_live")


(before, old_receipt), (after, new_receipt) = [verify_build(*b) for b in BUILDS]
assert "Success" in adb("install", str(before))
launch()
tap_required("START RACE")
time.sleep(3)
saved = checkpoint_hash()
select_overhead()
assert checkpoint_hash() == saved
screens = {}
capture_pair("01_qa1677_procedural", screens)
assert checkpoint_hash() == saved
adb("shell", "am", "force-stop", PKG)
# Install over the real older APK; never clear application data.
assert "Success" in adb("install", "-r", str(after))
assert checkpoint_hash() == saved
metadata = adb("shell", "dumpsys", "package", PKG)
assert re.search(r"versionCode=1678\b", metadata), metadata
launch()
assert not visible("START RACE"), "Upgrade did not resume game"
assert checkpoint_hash() == saved
tap_required("Match options")
assert visible("Woven board art: Overhead (QA)"), "Art selection was not preserved"
adb("shell", "input", "keyevent", "KEYCODE_BACK")
capture_pair("02_qa1678_atlas", screens)
assert checkpoint_hash() == saved
tap_required("Match options")
tap_required("Woven board art: Overhead (QA)")
capture_pair("03_qa1678_storybook", screens)
assert checkpoint_hash() == saved
# At the opening position the close camera may contain no replaced ornament.
# Require a changed whole-board image, but record local differences honestly.
atlas_view_changed = {
    view: screens["01_qa1677_procedural_" + view]["sha256"] !=
          screens["02_qa1678_atlas_" + view]["sha256"]
    for view in ("live", "full_map")
}
assert atlas_view_changed["full_map"], "No whole-board atlas difference"
for view in ("live", "full_map"):
    assert screens["02_qa1678_atlas_" + view]["sha256"] != screens["03_qa1678_storybook_" + view]["sha256"]
(EVIDENCE / "visual92_integrated_art_qa.json").write_text(json.dumps({
    "status": "CAPTURED_FOR_OWNER_REVIEW",
    "baseline": {"code": 1677, "source_commit": old_receipt["source_commit"], "apk_sha256": old_receipt["sha256"]},
    "candidate": {"code": 1678, "source_commit": new_receipt["source_commit"], "apk_sha256": new_receipt["sha256"]},
    "apk_atlas_sha256": ATLAS_SHA,
    "same_checkpoint_before_after": True,
    "checkpoint_sha256": saved,
    "art_selection_preserved": True,
    "atlas_view_changed": atlas_view_changed,
    "opening_live_difference_required": False,
    "screenshots": screens,
    "real_device_accepted": False,
    "human_art_approved": False
}, indent=2) + "\n")
print("VISUAL-92: same-race install-over comparison captured; human visual review pending")
