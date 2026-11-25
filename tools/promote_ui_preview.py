#!/usr/bin/env python3
"""Promote only existing shared-control artwork from the exact verified source ZIP."""
import hashlib
import json
from pathlib import Path
from import_ui_core_v1 import ROOT, collect
from check_crossroads_art_pack import parse_png

BUTTONS = {f"{family}_{state}.png" for family in ("primary", "secondary")
           for state in ("normal", "highlight", "pressed", "disabled")}
ICONS = {f"icon_{stem}.png" for stem in
         ("home", "multiplayer", "map", "journal", "settings", "dice", "expand", "collapse")}
SELECTED = BUTTONS | ICONS

def main():
    contract = json.loads((ROOT / "design/art/ui_core_v1_contract.json").read_text())
    source = ROOT / "design/art/imports/crossroads_ui_core_v1.zip"
    files = collect(source, contract)
    dest = ROOT / "app/src/main/assets/art/ui/v1"
    dest.mkdir(parents=True, exist_ok=True)
    unexpected = {p.name for p in dest.iterdir()} - SELECTED - {"manifest.json"}
    if unexpected:
        raise ValueError(f"Unexpected runtime UI assets: {unexpected}")
    entries = []
    for name in sorted(SELECTED):
        data = files[name]
        target = dest / name
        if target.exists() and target.read_bytes() != data:
            raise ValueError(f"Refusing to replace differing runtime asset: {name}")
        target.write_bytes(data)
        expected = (384, 144, 8, 6) if name in BUTTONS else (128, 128, 8, 6)
        if parse_png(target) != expected:
            raise ValueError(f"Invalid PNG: {name}")
        entries.append({"filename": name, "sha256": hashlib.sha256(data).hexdigest(),
                        "width": expected[0], "height": expected[1]})
    manifest = {"schemaVersion": 1, "reviewStatus": "qa-preview-not-owner-approved",
                "source": "design/art/imports/crossroads_ui_core_v1.zip", "assets": entries}
    (dest / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    print(f"Promoted {len(entries)} verified UI preview assets; native labels and fallback retained.")

if __name__ == "__main__":
    main()
