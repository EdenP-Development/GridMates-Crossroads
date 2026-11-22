# GAMEFEEL-58 — Readable, player-focused Crossroads playfield

**November 19, 2026.** QA-only draft stacked on UI-56 (#57) → UI-55 (#56) → UI-54 (#54) and the preceding unmerged visual stack. **Do not merge into main.**

## Real-phone problem
The owner tested immersive gameplay on an approximately 674×1536 portrait Android phone. The illustrated landscape looks beautiful, but the actual game is difficult to follow: routes are thin, traveler pieces are tiny even when zoomed, long stretches of scenery occupy the central view, and moving pieces/branch choices frequently lack clear visual focus. The encounter tray can cover a token at the bottom of the board. Inspiration is the legibility and sense of movement of polished mobile board games, **not copying Monopoly GO assets or mechanics**.

## Implemented / regression-safe
1. **Phase-driven close-up camera:** beginning roll 2.0×; subsequent rolling/travel 2.4×; path choice 2.15×; encounter 2.55×; target choice 2.3×. Finished races and read-only Full Map remain at 1×. Existing animated zoom and piece following remain intact.
2. **Playfield-safe camera focus:** the follow target sits at 43% of the viewport height during rolling/route selection, 38% during encounters, 40% during target choices—above the large bottom action tray. At map extremes, permit scenic world to extend behind the top HUD or bottom action dock rather than letting the active piece vanish beneath controls. User-driven manual pan/zoom retains the original bounds.
3. **Fork-centered decision framing:** bias camera 72% toward the actual fork origin and 28% toward the mean of legal exit coordinates. Players should see the branching decision, not a meaningless patch of meadow. Route geometry remains 100% engine-authored.
4. **Larger playable elements:** only in *live immersive gameplay*, scale piece radii and same-node offsets by 1.42×, trail art by 1.32× and node/junction medallions by 1.18×. The detailed backplate itself stays at its original aspect ratio. Read-only Full Map and Classic/non-immersive rendering retain their previous layout/scale.
5. **Readable roads:** the original atlas sprite remains intact, but its live-world draw width grows; a dark warm green engraved outline is placed underneath the illustrated road and stronger risk-tint trim around it. Fallback printed/simple roads get the same live-world size factor. Nothing is baked into the artwork or changes board graph.
6. **Choose-a-path spotlight:** while a legal human route choice is pending, gently darken the surroundings and draw wide, cream-edged outgoing legal roads and bright outlined exit nodes. The human can still tap the existing legal path geometry; accessible Compare paths remains. No targets during AI turns/transitions.
7. Existing tactile colored player rings, visible numeric seat badges, original animal portrait atlas, route event markers, step animations, landing glow, and the floating follow-camera button are all retained.

## What does NOT change
Board definitions, legal movement, dice/encounter odds, AI, rounds, LAN transport, local saves/checkpoint codec, achievements, artwork hashes, app ID/signing, and routes. Only Compose rendering and camera presentation parameters change.

## Automated acceptance
- New camera zoom/focus and size tests replace old 1.15/1.28× expectations.
- Edge-of-world portrait camera clamps keep piece inside the unobstructed region above HUD/dock; Full Map retains legacy camera math.
- Existing hit testing with zoom/pan, static artwork validation, release preflight, Android unit tests, lint, signed QA all must pass.

## Physical acceptance
- Install over previous QA without uninstalling; verify local record (7 solo wins/9 finished matches in owner's screenshots) and any saved game survive. Do not assert these counts remain current.
- Start a 3-player Woven Trail match: the active red piece should be recognizable without pinch-to-zoom, even while AI pieces are close together; individual numbers and colored rings must remain visible. Repeat with 4 players clustered at Start.
- Follow the traveler on a long straight path: confirm real-time camera tracking, no jump across HUD, no abrupt snap mid-path, and a clearly readable trail under the player.
- When arriving at a fork, see the active junction and immediately adjacent available branches. Tap each route or use Compare paths; ensure no route outside the engine's current legal list becomes selectable.
- Trigger Shelter/Hazard/Mischief: piece and active location should stay **above** the large encounter tray; board should not become a large empty grass field while the choice is open.
- Try Brookside Dash and Cedarwood Expedition with more/fewer nodes; artwork and highlights must not become overly thick or obscure choices on the dense Long course.
- Open Full Map: it should still display the complete board and all original compact nodes and pieces. Test free pan/pinch and one-tap follow button. Check large accessibility text.
- Check transitions under Cozy/Brisk pace, AI turn, pause, resume, results and LAN if available.

**No owner visual approval yet. The next APK is for on-phone comparison, not production.**
