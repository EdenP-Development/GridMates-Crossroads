#!/usr/bin/env python3
"""Import the exact generated V2 candidate from its supplied ZIP or PNG."""
import hashlib, json, sys, zipfile
from pathlib import Path
from check_crossroads_art_pack import parse_png, validate, ART, MANIFEST
EXPECTED = "779c42c21558a24ef9eb56f341d465ddeae68061389149bfe17fa00fe53525ab"
def main():
    if len(sys.argv) != 2:
        raise SystemExit("Usage: python3 tools/import_environment_v2.py <asset-pack.zip or atlas.png>")
    source = Path(sys.argv[1])
    if source.suffix.lower() == ".zip":
        with zipfile.ZipFile(source) as z:
            data = z.read("environment_atlas_v2.png")
    else:
        data = source.read_bytes()
    if hashlib.sha256(data).hexdigest() != EXPECTED:
        raise SystemExit("Atlas checksum mismatch; refusing import")
    manifest_bytes = MANIFEST.read_bytes()
    manifest = json.loads(manifest_bytes)
    slot = next(s for s in manifest["slots"] if s["id"] == "ENVIRONMENT_ATLAS_V2")
    target = ART / slot["filename"]
    prior = target.read_bytes() if target.exists() else None
    try:
        target.write_bytes(data)
        if parse_png(target) != (1024, 1024, 8, 6):
            raise ValueError("Expected 1024x1024 RGBA")
        slot.update(status="approved", license="generated", sha256=EXPECTED,
            origin="Original ChatGPT image generation, November 24 2026, twelve individual sprites; exact candidate checksum; see design/art/environment_v2_provenance.json.",
            reviewStatus="qa-preview-only-not-owner-device-approved")
        MANIFEST.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
        validate()
    except BaseException:
        if prior is None:
            target.unlink(missing_ok=True)
        else:
            target.write_bytes(prior)
        MANIFEST.write_bytes(manifest_bytes)
        raise
    print("V2 imported; run Android QA before claiming visual acceptance.")
if __name__ == "__main__":
    main()
