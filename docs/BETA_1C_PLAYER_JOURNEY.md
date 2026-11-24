# BETA-1C — Player journey usability pass

## Actual changes
Home now exposes Host nearby, Join nearby and How to play without expanding secondary settings. The original one-tap Solo default and course/seat selection remain. The pinned Start summary names the selected course and retains actual experimental rule status. A weighted scroll column and separately measured footer replace the fixed 126dp scroll padding and overlapping footer, so wrapped text and navigation insets do not hide the last settings. Shared tabletop utility controls are used for these links and the results navigation; rematch uses the shared primary action with existing host/guest eligibility.

No new network capability, changes to rules, profile schema, or production permissions. Nearby remains a two-phone plaintext trusted-network prototype. Storybook remains default. 

## Evidence and acceptance
QA1681 Android run 45316973684 passed; artifact 14886329061. This is the prior VISUAL-95 build, not this change or #96 course expansion. #96 Android run 45320370090 was pending at start of this pass.
New summary regression tests cover course, mode, seat count, active Fair Finish and blank fallback. Exact Android CI is required.

Physical review: short and large-font devices can reach every home option and Start; start all three courses in Solo and Pass & Play; verify help and Nearby discovery can be found without opening settings; confirm all turn phases, map return, results, view course, rematch, leave and offline recovery. Verify host/guest result action gating with two real phones. No main merge or beta release authorized.
