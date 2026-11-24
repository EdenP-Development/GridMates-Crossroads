# Identity 10 — artwork transfer and import

Both candidate ZIPs remain locally available as of November 24, 2026.
- environment archive SHA256 c14294767e47a1489baa4f4415bd18232310ed1c9295a9fbdcc13d7112e3c1cb; atlas SHA256 779c42c21558a24ef9eb56f341d465ddeae68061389149bfe17fa00fe53525ab.
- UI archive SHA256 80e8030c5520dde93eeb4a45eb56137b407a57a2c83d65fc0a944df0ff437085. Contains 24 PNGs and manifest. All individual files checked locally against manifest hashes and RGBA dimensions; alpha has transparent pixels and maxima 253–255, not all fully opaque. No stretch or device approval inferred.

In a checkout of feature/identity-10-import-ui, run:
    python3 tools/import_environment_v2.py /path/to/crossroads_environment_v2.zip
    python3 tools/import_ui_core_v1.py /path/to/crossroads_ui_core_v1.zip
    python3 tools/check_crossroads_art_pack.py
Then review git diff and add the environment PNG/manifest and design/art/library/ui_core_v1. Commit only on development branch, run Android QA. UI stays outside Android package until selected assets are explicitly registered and implemented. Native controls remain default. No binaries transferred by this commit; no new artwork or runtime UI change. QA1698 remains verified baseline.
