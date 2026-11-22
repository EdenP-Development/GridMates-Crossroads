#!/usr/bin/env python3
"""Validate optional, redistributable Crossroads artwork before Android packaging.

No third-party dependencies. 'planned' slots may be absent; an installed image
must be listed as 'approved', fingerprinted, correctly sized and attributed.
This check is not a substitute for human license review or visual QA.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import struct
import sys
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "app/src/main/assets/art/v1"
MANIFEST = ART / "manifest.json"
CATALOG = ROOT / "app/src/main/java/com/playereden/gridmates/crossroads/CrossroadsArtAssets.kt"
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
MAX_ASSET_BYTES = 8 * 1024 * 1024
MAX_TOTAL_BYTES = 32 * 1024 * 1024


def parse_png(path: Path) -> tuple[int, int, int, int]:
    data = path.read_bytes()
    if len(data) > MAX_ASSET_BYTES:
        raise ValueError("PNG exceeds 8 MiB per-asset budget")
    if not data.startswith(PNG_SIGNATURE):
        raise ValueError("not a PNG file")
    offset = len(PNG_SIGNATURE)
    header = None
    saw_end = False
    while offset + 12 <= len(data):
        length = struct.unpack_from(">I", data, offset)[0]
        name = data[offset + 4:offset + 8]
        end = offset + 12 + length
        if end > len(data):
            raise ValueError("truncated PNG chunk")
        contents = data[offset + 8:offset + 8 + length]
        crc_expected = struct.unpack_from(">I", data, offset + 8 + length)[0]
        if zlib.crc32(name + contents) & 0xFFFFFFFF != crc_expected:
            raise ValueError("bad PNG checksum")
        if offset == 8:
            if name != b"IHDR" or length != 13:
                raise ValueError("missing first IHDR chunk")
            width, height, depth, kind, comp, filt, interlace = struct.unpack(
                ">IIBBBBB", contents
            )
            header = (width, height, depth, kind)
            if comp or filt or interlace not in (0, 1):
                raise ValueError("unsupported PNG header")
        if name == b"IEND":
            if length != 0 or end != len(data):
                raise ValueError("unexpected trailing PNG data")
            saw_end = True
            break
        offset = end
    if header is None or not saw_end:
        raise ValueError("incomplete PNG")
    return header


def validate(require_all: bool = False) -> None:
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    if manifest.get("schemaVersion") != 1:
        raise ValueError("unsupported manifest schema")
    slots = manifest.get("slots")
    if not isinstance(slots, list) or not slots:
        raise ValueError("empty art slot manifest")
    code = CATALOG.read_text(encoding="utf-8")
    source_slots = re.findall(
        r'^\s+([A-Z][A-Z_0-9]+)\("([^"]+\.png)"\)[,;]?',
        code,
        flags=re.MULTILINE,
    )
    expected = dict(source_slots)
    if len(expected) != len(source_slots):
        raise ValueError("duplicate art slot in Kotlin code")

    actual_ids = set()
    names = set()
    approved = 0
    budget = 0
    for slot in slots:
        sid = slot["id"]
        name = slot["filename"]
        if sid in actual_ids or name in names:
            raise ValueError(f"duplicate art slot: {sid} / {name}")
        actual_ids.add(sid)
        names.add(name)
        if sid not in expected or expected[sid] != name:
            raise ValueError(f"manifest/code disagreement: {sid} / {name}")
        if not re.fullmatch(r"[a-z][a-z0-9_]*\.png", name):
            raise ValueError(f"unsafe art filename: {name}")
        width, height = slot["width"], slot["height"]
        if not (isinstance(width, int) and isinstance(height, int) and
                128 <= width <= 4096 and 128 <= height <= 4096):
            raise ValueError(f"invalid target size: {sid}")
        status = slot["status"]
        if status not in {"planned", "approved"}:
            raise ValueError(f"invalid status: {sid}")
        image = ART / name
        if status == "planned":
            if image.exists():
                raise ValueError(
                    f"{sid}: file exists but isn't approved with source rights and checksum"
                )
            if require_all:
                raise ValueError(f"{sid}: image not yet provided")
            continue
        if not image.is_file():
            raise ValueError(f"{sid}: approved PNG missing")
        if not isinstance(slot.get("origin"), str) or not slot["origin"].strip():
            raise ValueError(f"{sid}: origin/provenance must be documented")
        if slot.get("license") not in {"original", "commissioned", "generated", "licensed"}:
            raise ValueError(f"{sid}: rights category must be reviewed")
        sha = slot.get("sha256")
        if not isinstance(sha, str) or not re.fullmatch("[a-f0-9]{64}", sha):
            raise ValueError(f"{sid}: valid SHA256 required")
        if hashlib.sha256(image.read_bytes()).hexdigest() != sha:
            raise ValueError(f"{sid}: packaged bytes differ from approved checksum")
        found_width, found_height, depth, kind = parse_png(image)
        if (found_width, found_height) != (width, height):
            raise ValueError(
                f"{sid}: image is {found_width}x{found_height}, "
                f"manifest expects {width}x{height}"
            )
        if depth != 8 or kind not in (2, 6):
            raise ValueError(f"{sid}: must be 8-bit RGB or RGBA PNG")
        if slot["requiresAlpha"] and kind != 6:
            raise ValueError(f"{sid}: transparency required for miniature tokens")
        budget += image.stat().st_size
        approved += 1

    if actual_ids != set(expected):
        raise ValueError(f"slot registry mismatch: "
                         f"missing={sorted(set(expected) - actual_ids)}, "
                         f"extra={sorted(actual_ids - set(expected))}")
    unexpected = {p.name for p in ART.glob("*.png")} - names
    if unexpected:
        raise ValueError(f"unregistered PNG files: {sorted(unexpected)}")
    if budget > MAX_TOTAL_BYTES:
        raise ValueError("combined approved PNG size exceeds 32 MiB")
    print(f"ARTREADY-38: {approved}/{len(slots)} approved PNG slots, "
          f"{budget / 1048576:.2f} MiB; "
          "missing slots use the tested code-drawn fallbacks.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--require-all", action="store_true",
        help="Require all approved art at release acceptance"
    )
    args = parser.parse_args()
    try:
        validate(require_all=args.require_all)
    except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError) as exc:
        print(f"Art pack validation failed: {exc}", file=sys.stderr)
        sys.exit(1)
