#!/usr/bin/env python3
"""QA-79: verify two real QA builds install-over with an active match checkpoint."""
import base64
import os
import hashlib
import json
import xml.etree.ElementTree as ET
import pathlib
import re
import subprocess
import time

ROOT = pathlib.Path(__file__).resolve().parents[2]
BASE_CODE = int(os.environ.get("BASE_CODE", "1607"))
NEXT_CODE = int(os.environ.get("NEXT_CODE", "1613"))
BEFORE = ROOT / f"qa-archives/base/GridMates-Crossroads-QA-{BASE_CODE}.apk"
AFTER = ROOT / f"qa-archives/next/GridMates-Crossroads-QA-{NEXT_CODE}.apk"
PKG = "com.playereden.gridmates.crossroads.qa"
ACTIVITY = PKG + "/com.playereden.gridmates.crossroads.MainActivity"
PROFILE = "shared_prefs/crossroads_qa_profile_v1.xml"

def run(*args, stdin=None):
    result = subprocess.run(args, input=stdin, text=True, stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, timeout=120, check=True)
    print("COMMAND", " ".join(str(x) for x in args[:4]), "RESULT", result.stdout[-500:])
    return result.stdout

def adb(*args, stdin=None):
    return run("adb", *args, stdin=stdin)

def checksum(path):
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()

assert checksum(BEFORE) == os.environ.get("BASE_SHA", "b926e01bc0066a5e71140f9241f4b21f49e6aa799acaca2fef180b0679086dca")
assert checksum(AFTER) == os.environ.get("NEXT_SHA", "c42054e446c35e0b805ac69950e1ee478c39eb0058d32ff0290e8389f0f94779")

# The workflow downloads immutable real APK archives, not a fresh local build.
# Verify the two GitHub receipt identities, version codes and signer agree.
receipts = []
for path, code in [(BEFORE, BASE_CODE), (AFTER, NEXT_CODE)]:
    receipt = json.loads((path.parent / "GridMates-Crossroads-QA-receipt.json").read_text())
    assert receipt["channel"] == "public-test-only-qa"
    assert receipt["application_id"] == PKG
    assert receipt["version_code"] == code and receipt["sha256"] == checksum(path)
    receipts.append(receipt)
assert NEXT_CODE > BASE_CODE, "Install-over must increase versionCode"
assert receipts[0]["signer_cert_sha256"] == receipts[1]["signer_cert_sha256"], (
    "Signing identity changed between QA builds"
)

def read_prefs():
    return adb("shell", "run-as", PKG, "cat", PROFILE)

def values(raw):
    return {item.get("name"): item.text if item.tag == "string" else item.get("value")
            for item in ET.fromstring(raw) if item.get("name")}

def ui_xml():
    adb("shell", "uiautomator", "dump", "/sdcard/crossroads-window.xml")
    return ET.fromstring(adb("shell", "cat", "/sdcard/crossroads-window.xml"))

def tap_required(label):
    for attempt in range(5):
        tree = ui_xml()
        for node in tree.iter("node"):
            txt = (node.get("text", "") + " " + node.get("content-desc", "")).upper()
            if label.upper() not in txt:
                continue
            match = re.search(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
            if match:
                x1, y1, x2, y2 = map(int, match.groups())
                adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
                return
        time.sleep(1)
    raise AssertionError(f"Could not tap required Android control {label}")

assert "Success" in adb("install", str(BEFORE))
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(3)
adb("shell", "am", "force-stop", PKG)

# Seed legitimate Android SharedPreferences XML in this debug-only app's sandbox.
xml = """<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <int name="completed" value="7" />
    <int name="wins" value="3" />
</map>
"""
adb("shell", "run-as", PKG, "mkdir", "-p", "shared_prefs")
# A single quoted remote shell command keeps the redirection under the app UID.
encoded = base64.b64encode(xml.encode("utf-8")).decode("ascii")
adb("shell", f"run-as {PKG} sh -c 'echo {encoded} | base64 -d > {PROFILE}'")
old = adb("shell", "run-as", PKG, "cat", PROFILE)
assert "<int name=\"completed\" value=\"7\" />" in old, repr(old)
assert "<int name=\"wins\" value=\"3\" />" in old, repr(old)

# Create a *real* unfinished Classic match in QA1607. No fabricated
# checkpoint JSON or test-only secret preference bypass is accepted.
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(3)
tap_required("START RACE")
time.sleep(3)
tap_required("ROLL THE DIE")
time.sleep(4)
adb("shell", "am", "force-stop", PKG)
old = read_prefs()
original = values(old)
assert original["completed"] == "7" and original["wins"] == "3"
assert original.get("active_match_checkpoint_v1"), (
    "The older installed APK did not commit the active match"
)
checkpoint = json.loads(original["active_match_checkpoint_v1"])
assert checkpoint["formatVersion"] == 1
assert checkpoint["actions"], "No real game action was saved"
assert checkpoint["expected"]["phase"] != "FINISHED"
print("Old saved race:", checkpoint["boardId"], "actions", len(checkpoint["actions"]))

# This must be an actual install-over. Do not clear data or uninstall.
assert "Success" in adb("install", "-r", str(AFTER))
new = adb("shell", "run-as", PKG, "cat", PROFILE)
assert old.strip() == new.strip(), "Profile bytes changed during APK install-over"
metadata = adb("shell", "dumpsys", "package", PKG)
assert re.search(rf"versionCode={NEXT_CODE}\b", metadata), f"Expected upgraded versionCode {NEXT_CODE}"
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(3)
pid = adb("shell", "pidof", PKG).strip()
assert pid and pid.isdigit(), "Updated app failed to remain running"
# The upgraded app has to resume the existing race, not merely retain
# on-disk XML before launch. Do not erase saved stats or checkpoint.
ui = ui_xml()
on_screen = " ".join(
    node.get("text", "") + " " + node.get("content-desc", "")
    for node in ui.iter("node")
).upper()
assert "START RACE" not in on_screen, (
    "Updated APK fell back to the menu instead of resuming a valid saved match"
)
assert any(mark in on_screen for mark in ("ROLL THE DIE", "ROUND", "TURN", "CHOOSE")), (
    "Updated app did not expose a recognizable resumed game HUD"
)
adb("shell", "am", "force-stop", PKG)
after_launch = values(read_prefs())
assert after_launch["active_match_checkpoint_v1"] == original["active_match_checkpoint_v1"], (
    "Updated APK changed/lost the original in-progress checkpoint"
)
assert after_launch["completed"] == "7" and after_launch["wins"] == "3"
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(2)
print(f"PASS: QA {BASE_CODE} -> QA {NEXT_CODE} real emulator install-over; "
      "original roll/checkpoint restored and stats preserved.")

# Evidence includes a real emulator screenshot and a verifiable, nonsecret
# upgrade receipt; neither substitutes for owner-phone install-over acceptance.
evidence = ROOT / "qa-evidence"
evidence.mkdir(exist_ok=True)
with (evidence / f"crossroads-{NEXT_CODE}-resumed-game.png").open("wb") as out:
    subprocess.run(["adb", "exec-out", "screencap", "-p"],
                   check=True, timeout=60, stdout=out)
(evidence / f"crossroads-{BASE_CODE}-to-{NEXT_CODE}-upgrade.json").write_text(
    json.dumps({
        "status": "PASS",
        "emulator_api": 35,
        "baseline_version": BASE_CODE,
        "candidate_version": NEXT_CODE,
        "baseline_source_commit": receipts[0]["source_commit"],
        "candidate_source_commit": receipts[1]["source_commit"],
        "same_test_signer": True,
        "active_checkpoint_sha256": hashlib.sha256(
            original["active_match_checkpoint_v1"].encode("utf-8")
        ).hexdigest(),
        "saved_action_count": len(checkpoint["actions"]),
        "board_id": checkpoint["boardId"],
        "stats_completed": int(after_launch["completed"]),
        "stats_wins": int(after_launch["wins"]),
        "resumed_in_game": True,
        "owner_phone_verified": False,
    }, indent=2) + "\\n"
)
