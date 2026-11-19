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
require('Text("About & data")' in home,
        "Home must disclose local saves and nearby play")
require('if (BuildConfig.DEBUG)' in home and 'if (BuildConfig.DEBUG)' in lobby,
        "QA screen footers must be debug-only")
# Both the original labeled header and the newer course-only game HUD are
# acceptable: neither can expose a QA label in a public match screen.
legacy_debug_title = 'if (BuildConfig.DEBUG) "CROSSROADS · QA" else "CROSSROADS"' in activity
course_only_hud = ('Text(CrossroadsCourse.title(board.id)' in activity and
                   '"CROSSROADS · QA"' not in activity)
require(legacy_debug_title or course_only_hud,
        "In-match HUD must show no unconditional QA branding in public UI")
require('"crossroads_qa_profile_v1"' in activity,
        "Legacy local profile key changed; QA updates could lose progress")

if failures:
    print("RELEASE-25 preflight FAILED:")
    for problem in failures:
        print(" - " + problem)
    sys.exit(1)
print("RELEASE-25 public-presentation preflight passed")
print("Note: production signing, legal/privacy review and physical QA remain outstanding.")
