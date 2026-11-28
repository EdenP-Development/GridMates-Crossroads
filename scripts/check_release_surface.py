#!/usr/bin/env python3
"""Static guardrail for public-facing Android presentation.

This is a source preflight, not physical phone QA, a signing approval or a
privacy-policy compliance audit. It preserves the separately signed .qa channel.
"""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
a = "{http://schemas.android.com/apk/res/android}"
failures = []


def require(condition, detail):
    if not condition:
        failures.append(detail)


manifest_path = root / "app/src/main/AndroidManifest.xml"
manifest = ET.parse(manifest_path).getroot()
network_permissions = {
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.ACCESS_WIFI_STATE",
    "android.permission.CHANGE_WIFI_MULTICAST_STATE",
}
def declared_permissions(doc):
    return {node.get(a + "name") for node in doc.findall("uses-permission")}
require(not (declared_permissions(manifest) & network_permissions),
        "Public main manifest must not request Nearby network permissions")
debug_manifest = ET.parse(root / "app/src/debug/AndroidManifest.xml").getroot()
require(network_permissions <= declared_permissions(debug_manifest),
        "Debug QA must retain all Nearby network permissions")

app = manifest.find("application")
require(app is not None, "Android application node is missing")
if app is not None:
    require(app.get(a + "label") == "@string/app_name",
            "Launcher label must use the per-build resource, not a QA literal")
    require(app.get(a + "icon") == "@mipmap/ic_launcher",
            "Launcher icon must use the Crossroads adaptive icon")
    require(app.get(a + "allowBackup") == "false",
            "Backup behavior was changed without a reviewed migration")

build = (root / "app/build.gradle.kts").read_text()
require('applicationId = "com.playereden.gridmates.crossroads"' in build,
        "Public package ID changed; release migration needs explicit approval")
require('applicationIdSuffix = ".qa"' in build,
        "QA suffix missing: risk installing over the wrong application")
require('resValue("string", "app_name", "GridMates: Crossroads")' in build,
        "Public app label must be GridMates: Crossroads")
require('resValue("string", "app_name", "Crossroads QA")' in build,
        "QA app must remain visibly distinguishable from public")
require('signingConfig = signingConfigs.getByName("qaPublicTestOnly")' in build,
        "Existing QA signing setup changed; update-over may no longer work")
release_section = build.split('getByName("release")', 1)
require(len(release_section) == 2, "Missing public release build configuration")
if len(release_section) == 2:
    require("qaPublicTestOnly" not in release_section[1],
            "Public release must never reference the publicly known QA key")

icon = root / "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml"
vector = root / "app/src/main/res/drawable/ic_crossroads_foreground.xml"
colors = root / "app/src/main/res/values/colors.xml"
for p in (icon, vector, colors):
    require(p.is_file(), f"Missing launcher asset: {p.relative_to(root)}")
    if p.is_file():
        ET.parse(p)

home = (root / "app/src/main/java/com/playereden/gridmates/crossroads/CrossroadsHome.kt").read_text()
lobby = (root / "app/src/main/java/com/playereden/gridmates/crossroads/CrossroadsLanLobby.kt").read_text()
activity = (root / "app/src/main/java/com/playereden/gridmates/crossroads/MainActivity.kt").read_text()
# The entrypoint may use the shared tabletop control instead of a bare Text.
require(('Text("About & data")' in home or
         'text = "About & data", onClick = { showAbout = true }' in home)
        and 'if (showAbout)' in home
        and 'SAVING YOUR PROGRESS' in home
        and 'The current nearby connection is not encrypted' in home,
        "Home must disclose local saves and nearby play")
require('if (BuildConfig.DEBUG)' in home and 'if (BuildConfig.DEBUG)' in lobby,
        "QA screen footers must be debug-only")
require('if (BuildConfig.DEBUG) "CROSSROADS · QA" else "CROSSROADS"' in activity,
        "In-match QA title must not appear in the public UI")
require('"crossroads_qa_profile_v1"' in activity,
        "Legacy local profile key changed; QA updates could lose progress")

require("if (!BuildConfig.DEBUG) original else original +" in activity,
        "Public recovery registry must exclude experimental board variants")
require("selectedCourseId, BuildConfig.DEBUG && playtestRules, BuildConfig.DEBUG && fairFinishExperiment" in activity,
        "Public new sessions must force original rules")
require("if (BuildConfig.DEBUG) CrossroadsNearby(context)" in activity,
        "Nearby initialization must be debug-only")
for entry in ("fun startLanHost(requireCode: Boolean, seats: Int) {",
              "fun joinLan(host: String, port: Int, code: String, resume: Boolean = false) {"):
    require(entry + "\n        if (!BuildConfig.DEBUG) return" in activity,
            "Nearby entry point is missing public guard: " + entry)
require('if (BuildConfig.DEBUG) {\n        Text("Nearby · Two phones on the same Wi-Fi"' in home,
        "Public home must hide Nearby entry points")
require("if (BuildConfig.DEBUG) {\n        TabletopPanel(\n            color = if (playtestRules)" in home,
        "Public home must hide tactical rule controls")

if failures:
    print("RELEASE-25 preflight FAILED:")
    for problem in failures:
        print(" - " + problem)
    sys.exit(1)
print("RELEASE-25 public-presentation preflight passed")
print("Note: production signing, legal/privacy review and physical QA remain outstanding.")
