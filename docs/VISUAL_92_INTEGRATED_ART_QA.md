# VISUAL-92 — Integrated artwork evidence and install-over comparison

QA1675, QA1676, QA1677 and QA1678 all completed Android build, tests, lint and signed QA packaging successfully. QA1678 is the first candidate containing the actual environment atlas. These facts do not establish physical-device or artistic approval.

## Exact candidates

- QA1677: Android run 45308168694, artifact 14883685035, source caf832d4a57ad5489437935aac6d1de3fe6bc923. Environment atlas slot exists, but no PNG: procedural scenery.
- QA1678: Android run 45309598813, artifact 14884620025, source 6f173a4afc9c2adafee2475f35ff0522efcdbbb1. Environment atlas PNG SHA-256 19819a342ecaf9249500406ded7d9ebcda3d101874168e8ec777846985cc8a49.

The new workflow downloads those exact artifact names from the pinned runs, validates the receipts against source commit and actual APK bytes, verifies the APK signature with apksigner, and checks the atlas inside the candidate APK (absent in baseline, exact checksum and 1024x1024 8-bit RGBA in candidate).

## Same-race comparison

Start one genuine Woven race on QA1677, select Overhead, and capture Full Map and live board. Install QA1678 over the existing app, without uninstalling or clearing storage. Verify the checkpoint hash before and after upgrade, version code and persisted appearance setting. Capture the same two views in the candidate. Switch to Storybook, capture both views again, and confirm unchanged checkpoint. Upload six actual PNGs with dimensions, checksums and machine-readable receipt.

No new game code or artwork is added. The harness does not assert artistic quality from different screenshot hashes. An emulator at one profile and the opening race position does not prove later-junction framing, real-device performance, two-phone LAN, or all historical save upgrades. It does not prove that every atlas cell was actually drawn; candidate art must be inspected visually. No main merge.

## Owner review

Evaluate visual scale and contrast at normal zoom, how the trees/cottages/flowers fit the graph, path readability, cohesion with pawns and controls, and whether richer hand-illustrated assets are needed. Preserve Storybook default pending approval. The two earlier generated presentation posters are concepts, not screenshots or evidence of a passed build. Next: inspect the actual six captures, then prioritize artwork changes over additional infrastructure. Physical QA1678 install-over, real match pacing and two-phone multiplayer remain separate gates.
