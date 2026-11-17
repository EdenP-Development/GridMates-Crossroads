# Prototype rules specification (NOT LOCKED)
**Purpose:** create the smallest complete race to test the signature crossroads mechanism.

## Setup
- 2–4 total player seats, human or bot; minimum one local human in Android solo. One shared Start and Finish.
- All players see the complete course and the risk labels at junctions.
- A single standard six-sided die controls movement in the first experiment (not a final decision).

## Turn sequence
1. Host/runtime chooses dice value 1–6; advance through connected spaces.
2. Reaching a fork suspends movement until the active player chooses a visible route; any remaining steps continue after the choice.
3. On ending the turn on an eligible event space, draw one event from its region pool (not a permanent printed effect). Resolve the event and legal target selection.
4. Evaluate finish; move clockwise to the next seat.

## Win condition
- First participant reaching or passing Finish wins. If a setback moves a player behind Start, clamp at Start.
- No player elimination, inventory loss, turns skipped, or real-money resources.

## Event safety candidates
- Max one random event drawn during a turn; event-induced movement does not recursively trigger further events.
- Bump back 2, boost forward 1–2, one-use shield, optional random challenge. Caps must be tested.
- An opponent's protected token consumes its shield and prevents one setback.
- Targeting may be restricted to players ahead to reduce repeated harassment; test before locking.
- Positive/negative event frequencies vary by region but disclose broad risk before route selection.

## Resolution and determinism
- Legal move transition takes `state + action + dice + random draw => new state + log`; no UI/network dependency.
- Fork direction and target selection must be explicit actions.
- Each turn increments an action/turn sequence number. LAN clients never independently roll or draw.
- On win, lock the session outcome once; persist results exactly once.

## Key test cases
- All fork options lead eventually to Finish.
- A six that crosses two junctions can ask two consecutive route choices without losing remaining steps.
- End-of-turn effects never create infinite chains or route softlocks.
- Multiple players can occupy shared spaces safely; reconnect doesn't replay events twice.
- Difficulties differ in bot decision quality, not hidden favorable dice.
