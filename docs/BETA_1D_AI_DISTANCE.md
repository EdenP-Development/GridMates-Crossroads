# BETA-1D — AI audit and distance reuse

Current bots already evaluate public graph distance, risk, relative lead, shields and rival position; seeded route jitter shares the authoritative random stream. BOT commands are replayed rather than storing their resolved choices. Consequently a behavior or RNG change would invalidate some historical checkpoints unless separately versioned.

This pass caches shortest graph distances per session without changing scoring, route iteration, random draws, encounter choices or target selection. Invalid cycles fall back to the legacy guarded recursion. No timing improvement is asserted until measured. New tests compare all nodes on all three boards and six rule versions against the old algorithm, including malformed cyclic input. Existing deterministic recovery, rematch and balance tests remain mandatory.

Candidate QA pending. QA1685 from #98 is verified; #99 route clarity pending at start. No main merge. Follow up with versioned AI experiments only if human testing reveals a meaningful deficiency; otherwise prioritize nearby reliability and beta validation.
