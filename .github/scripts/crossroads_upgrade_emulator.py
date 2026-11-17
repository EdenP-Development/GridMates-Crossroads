#!/usr/bin/env python3
"""Verify *two exact separate GitHub-built APKs* install-over without losing profile records."""
import hashlib
import pathlib
import re
import subprocess
import time

ROOT = pathlib.Path(__file__).resolve().parents[2]
BEFORE = ROOT / "qa-archives/v1008/GridMates-Crossroads-QA-1008.apk"
AFTER = ROOT / "qa-archives/v1009/GridMates-Crossroads-QA-1009.apk"
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

assert checksum(BEFORE) == "6cb6e89128d7f3a5ed123f38ebd5878f2775e200898f6ee8a4379dbbdf01354d"
assert checksum(AFTER) == "78ed337d05b9c6ea0d01f00d464ecf9eba6c164557a6b7e7e9b2cad2ada21d3b"

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
adb("shell", "run-as", PKG, "sh", "-c", "cat > " + PROFILE, stdin=xml)
old = adb("shell", "run-as", PKG, "cat", PROFILE)
assert "<int name=\"completed\" value=\"7\" />" in old, repr(old)
assert "<int name=\"wins\" value=\"3\" />" in old, repr(old)

# This must be an actual install-over. Do not clear data or uninstall.
assert "Success" in adb("install", "-r", str(AFTER))
new = adb("shell", "run-as", PKG, "cat", PROFILE)
assert old.strip() == new.strip(), "Profile bytes changed during APK install-over"
metadata = adb("shell", "dumpsys", "package", PKG)
assert re.search(r"versionCode=1009\\b", metadata), "Expected upgraded versionCode 1009"
adb("shell", "am", "start", "-W", "-n", ACTIVITY)
time.sleep(3)
pid = adb("shell", "pidof", PKG).strip()
assert pid and pid.isdigit(), "Updated app failed to remain running"
print("PASS: exact SHA-pinned 1008 -> 1009 in-place upgrade, preserved QA stats file, new app launched.")
