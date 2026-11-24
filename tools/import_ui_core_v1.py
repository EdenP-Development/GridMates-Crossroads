#!/usr/bin/env python3
"""Import the reviewed UI source library, without adding unused runtime assets."""
import hashlib
import json
from pathlib import Path
import struct
import sys
import zipfile
import tempfile

ROOT = Path(__file__).resolve().parents[1]
EXPECTED = "80e8030c5520dde93eeb4a45eb56137b407a57a2c83d65fc0a944df0ff437085"
def sha(data):
    return hashlib.sha256(data).hexdigest()

def collect(source, contract):
    if sha(source.read_bytes()) != EXPECTED:
        raise ValueError("UI archive differs from exact reviewed candidate")
    expected = {a["id"]: a for a in contract["assets"]}
    with zipfile.ZipFile(source) as z:
        names = z.namelist()
        wanted = {"manifest.json"} | {a["filename"] for a in expected.values()}
        if len(names) != len(wanted) or set(names) != wanted:
            raise ValueError("Unexpected or duplicate archive entries")
        if sum(i.file_size for i in z.infolist()) > 4 * 1024 * 1024:
            raise ValueError("Archive exceeds source-library budget")
        manifest = json.loads(z.read("manifest.json"))
        entries = manifest["assets"]
        if len(entries) != len(expected) or {a["id"] for a in entries} != set(expected):
            raise ValueError("Asset manifest IDs differ from contract")
        output = {"manifest.json": z.read("manifest.json")}
        for a in entries:
            spec = expected[a["id"]]
            if (a["file"], a["width"], a["height"]) != (spec["filename"], spec["width"], spec["height"]):
                raise ValueError("Asset metadata mismatch: " + a["id"])
            data = z.read(a["file"])
            if sha(data) != a["sha256"]:
                raise ValueError("Asset hash mismatch: " + a["id"])
            if data[:16] != b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR" or struct.unpack(">IIBB", data[16:26]) != (a["width"], a["height"], 8, 6):
                raise ValueError("Expected exact 8-bit RGBA dimensions")
            output[a["file"]] = data
        return output

def main():
    if len(sys.argv) != 2:
        raise SystemExit("Usage: python3 tools/import_ui_core_v1.py <crossroads_ui_core_v1.zip>")
    contract = json.loads((ROOT / "design/art/ui_core_v1_contract.json").read_text())
    files = collect(Path(sys.argv[1]), contract)
    dest = ROOT / "design/art/library/ui_core_v1"
    if dest.exists():
        if {p.name for p in dest.iterdir()} == set(files) and all((dest/n).is_file() and (dest/n).read_bytes() == b for n,b in files.items()):
            print("Exact UI source library already imported.")
            return
        raise SystemExit("Destination contains different files; refusing overwrite")
    dest.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=dest.parent) as temp:
        staged = Path(temp) / "ui_core_v1"
        staged.mkdir()
        for name, data in files.items():
            (staged / name).write_bytes(data)
        staged.rename(dest)
    print("Imported 24 UI source candidates. No runtime packaging or visual approval implied.")

if __name__ == "__main__":
    main()
