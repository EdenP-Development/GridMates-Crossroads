# QA and playtest policy
**Planning data validation is not gameplay QA.** Keep evidence separate for desktop simulation, Android devices and real LAN play.

## Design-data validation
- [ ] All nodes and event IDs are unique; all edges point to real nodes.
- [ ] Exactly one Start and Finish, all branches reach Finish, no unplanned loops.
- [ ] Forks have at least two distinct outgoing choices and meaningful risk-length tradeoffs.
- [ ] Every region has eligible events; event effect bounds are enforced.
- [ ] Handcrafted topology stays unchanged between seeded matches.

## Engine and AI milestone
- [ ] For each legal roll, never lose pending steps or skip a required fork decision.
- [ ] Illegal target, wrong-turn action, repeated action and bad version are rejected safely.
- [ ] A complete match produces exactly one winner; can replay from same seed/actions.
- [ ] Random events do not recursively trigger themselves forever.
- [ ] Large bot-only simulation sample has 0 softlocks and includes all legal route options.
- [ ] Bot logic doesn't cheat dice or hidden outcomes.

## Human experience
- [ ] At least 12 human pilot games, mixed player counts and different starting positions.
- [ ] Record times, decisions, event frequency, win margin and rematch willingness.
- [ ] Track losing player's frustration after sabotage and event repetition.
- [ ] User can explain route risk before choosing; board legible at phone size.
- [ ] Test quick/long target durations empirically; edit or remove modes that fail.

## Android / LAN future gates
- [ ] Android package independently installs, launches, suspends/resumes and retains saves.
- [ ] At least 2 physical Android phones join/finish; 3–4 seat scenarios tested.
- [ ] Host disconnect, client reconnect, Wi-Fi changes, LAN permission denial and app backgrounding have documented outcomes.
- [ ] No unexplained event divergence among clients.
- [ ] Accessibility: scalable text, contrast, non-color player indicators and reduced motion.

## Result record template
Date/build/commit; device models+Android; scenario; actual vs expected; screenshots/log links; PASS/FAIL/BLOCKED; known limitations; owner decision.
