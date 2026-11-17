#!/usr/bin/env python3
"""Verify *two exact separate GitHub-built APKs* install-over without losing profile records."""
import base64
import os
import hashlib
import pathlib
import re
import subprocess
import time

ROOT = pathlib.Path(__file__).resolve().parents[2]
BASE_CODE = int(os.environ.get("BASE_CODE", "1026"))
NEXT_CODE = int(os.environ.get("NEXT_CODE", "1037"))
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

assert checksum(BEFORE) == os.environ.get("BASE_SHA", "8afa05795a0f00e4259d6d2d85e7a5f4b20a0e04df3160bc7899daceac8c4704")
assert checksum(AFTER) == os.environ.get("NEXT_SHA", "1dd8e270cbc14823f91432d3635e4f87529f19b7c6b7f377be4ce9b5db9bc0c9")

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
print(f"PASS: exact SHA-pinned {BASE_CODE} -> {NEXT_CODE} in-place upgrade, preserved QA stats file, new app launched.")

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
        if tap_text("Roll the die"):
            time.sleep(2)
            screenshot(f"crossroads-{NEXT_CODE}-first-roll.png")
    else:
        print("INFO: Could not match Compose button in UIAutomator; main-menu screenshot is still available.")
except Exception as exc:
    print("INFO: Non-gating screenshot interaction unavailable:", repr(exc))
