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
            match = re.search(r"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]", node.get("bounds", ""))
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

# Collect screenshot evidence for UI review (not a full layout acceptance test).
evidence = ROOT / "qa-evidence"
evidence.mkdir(exist_ok=True)
def screenshot(name):
    subprocess.run(["adb", "exec-out", "screencap", "-p"],
                   check=True, timeout=60, stdout=(evidence / name).open("wb"))
screenshot(f"crossroads-{NEXT_CODE}-menu.png")
try:
    # Android UIAutomator exposes merged Compose button labels; tap the 2-player button.
    import xml.etree.ElementTree as ET
    def dump_ui():
        adb("shell", "uiautomator", "dump", "/sdcard/crossroads-window.xml")
        xml = adb("shell", "cat", "/sdcard/crossroads-window.xml")
        return ET.fromstring(xml)
    def tap_text(label):
        tree = dump_ui()
        for node in tree.iter("node"):
            if label in (node.attrib.get("text", "") + " " + node.attrib.get("content-desc", "")):
                m = re.search(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.attrib.get("bounds", ""))
                if m:
                    x1,y1,x2,y2=map(int,m.groups())
                    adb("shell","input","tap",str((x1+x2)//2),str((y1+y2)//2))
                    return True
        return False
    if tap_text("2 players"):
        time.sleep(2)
        screenshot(f"crossroads-{NEXT_CODE}-board.png")
        if tap_text("Overview"):
            time.sleep(0.8)
            screenshot(f"crossroads-{NEXT_CODE}-overview.png")
        if tap_text("Follow"):
            time.sleep(0.8)
            screenshot(f"crossroads-{NEXT_CODE}-follow.png")
        if tap_text("Zoom in board"):
            time.sleep(0.8)
            screenshot(f"crossroads-{NEXT_CODE}-zoom-in.png")
            tap_text("Follow")
        if tap_text("Roll the die"):
            time.sleep(2)
            screenshot(f"crossroads-{NEXT_CODE}-first-roll.png")
    else:
        print("INFO: Could not match Compose button in UIAutomator; main-menu screenshot is still available.")
except Exception as exc:
    print("INFO: Non-gating screenshot interaction unavailable:", repr(exc))
