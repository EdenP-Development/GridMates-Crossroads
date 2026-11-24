# Polish 02 — woodland groundcover

QA1689 from Polish 01 passed Android QA (45427373772, artifact 14938610874). Physical camera approval remains outstanding.

New optional overhead scenery uses a deterministic jittered grid of up to 400 candidate small vegetation patches per course. Each is screened against graph edges, nodes, existing landmarks and other patches. Forest zones have denser foliage; open meadow areas have more restrained flower accents. No changes to Storybook, atlas bytes, course rules, camera, checkpoint, or public default. Both live and full-map overhead renderers use the same normalized positions. Planning is remembered per board. Rendering adds several small strokes per accepted patch; actual phone frame rate is not measured.

Tests check actual three-course clearance, size and determinism. Android QA and owner visual review pending. No main merge. Next: check natural-looking route integration and compare actual overhead against Storybook on phone; don't approve art based on procedural code alone.
