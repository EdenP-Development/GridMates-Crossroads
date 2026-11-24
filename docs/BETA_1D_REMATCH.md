# BETA-1D — Rematch integrity

Offline Race Again previously called the home-screen start path, which reconstructed course/rules/human seats from current preferences. Restored matches, including historical lab variants, deserve a rematch of the completed race, not a silently different game. GameSession.freshRematch(seed) now requires a finished session and preserves its authoritative board/catalogue and original human seat assignment; it creates a fresh seed, empty action history and initial state. Offline and host paths use it. Existing match IDs, timer reset, stats persistence, LAN publication and guest restrictions remain.

The existing 144-match recovery matrix now checks fresh rematches after restoring completed races across all three courses, six immutable rules versions, Solo/Pass & Play and two/four seats. Unfinished rematch is rejected. Exact CI pending.

Starting-seat fairness remains a separate design choice. GameSession replay initializes seat zero, the Solo record assumes seat zero is human, and v5 uses seat ordering to determine final chance. Do not rotate physical seats or change rules without explicit versioning and accounting review. Original dice, board topology, saved checkpoint schema and networking protocol are unchanged.

Prior QA1682 is verified; BETA-1C release preflight was corrected to recognize About & data shared control while requiring substantive local-save and plaintext-LAN disclosure. No main merge, public release or physical LAN acceptance.
