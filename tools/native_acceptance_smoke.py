"""Synthetic QA interaction smoke; no private data or public signing."""
import json
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

out = Path(sys.argv[1])
PACKAGE = "com.playereden.gridmates.crossroads.qa"

def adb(*args):
    return subprocess.check_output(["adb", *args], timeout=40)

def hierarchy():
    adb("shell", "uiautomator", "dump", "/sdcard/crossroads-scene.xml")
    raw = adb("exec-out", "cat", "/sdcard/crossroads-scene.xml")
    return raw, ET.fromstring(raw)

def find(root, label):
    return [n for n in root.iter("node") if n.get("package") == PACKAGE and
            label in (n.get("text"), n.get("content-desc"))]

def wait(label, seconds=35):
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        raw, root = hierarchy()
        nodes = find(root, label)
        if nodes:
            return raw, nodes[0]
        time.sleep(1)
    raise RuntimeError("Expected visible UI label missing: " + label)

def tap(label):
    _, node = wait(label)
    x1,y1,x2,y2 = map(int,re.findall(r"\d+",node.attrib["bounds"]))
    if x2 <= x1 or y2 <= y1:
        raise RuntimeError("Empty bounds: " + label)
    adb("shell","input","tap",str((x1+x2)//2),str((y1+y2)//2))

def capture(name,label):
    wait(label)
    time.sleep(1)
    raw,_ = wait(label)
    png=adb("exec-out","screencap","-p")
    if len(png)<10000 or not png.startswith(bytes.fromhex("89504e470d0a1a0a")):
        raise RuntimeError("Invalid capture: " + name)
    (out/(name+".xml")).write_bytes(raw)
    (out/(name+".png")).write_bytes(png)
    results.append({"scene":name,"expected_visible_label":label})
    (out/"interaction-receipt.json").write_text(json.dumps(results,indent=2)+"\n")

results=[]
capture("01-home","START RACE")
tap("Brookside Dash, Quick")
# Selection confirmed by the persistent summary or course description containing Brookside.
deadline=time.monotonic()+25
while time.monotonic()<deadline:
    raw,root=hierarchy()
    if any("Brookside" in (n.get("text") or "") for n in root.iter("node")):
        break
    time.sleep(1)
else:
    raise RuntimeError("Quick course summary did not update")
capture("02-quick-selected","START RACE")
tap("START RACE")
capture("03-active-race","Full map")
tap("Full map")
# Map dialog labels are captured for inspection; do not claim full-map semantics yet.
time.sleep(2)
raw,root=hierarchy()
if not any(n.get("package")==PACKAGE for n in root.iter("node")):
    raise RuntimeError("Crossroads not visible after map tap")
(out/"04-map-attempt.xml").write_bytes(raw)
(out/"04-map-attempt.png").write_bytes(adb("exec-out","screencap","-p"))
results.append({"scene":"04-map-attempt","status":"tap executed; visual and hierarchy review required"})
(out/"interaction-receipt.json").write_text(json.dumps(results,indent=2)+"\n")
