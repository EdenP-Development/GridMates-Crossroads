# BETA-1E — Nearby connection lifecycle

Prior QA1687 passed Android and rules/design workflows (45328504605 and 45328504716). New transport candidate requires its own exact-head checks.

Guest sockets are registered before connect/hello, allowing close to cancel an unfinished handshake. Closed guests cannot accept a late welcome. Host and guest writer tasks capture their original socket and reject stale tasks rather than send through a replacement socket. Executor shutdown races during sends are tolerated. Existing room code, reserved token, match identity, game rules, save format and protocol remain unchanged. New real loopback regression withholds welcome and confirms cancel closes the socket with no state or connection callbacks. Existing private room, rejoin, duplicate-action and rematch tests remain required.

Physical beta gate: two phones on trusted household Wi-Fi; discover room, join by code, cancel during join, reconnect using reserved token, compare both screens after every action, finish and rematch, background/foreground and Wi-Fi interruption. Manual IPv4 fallback should work. Existing TCP is unencrypted; no Internet play, host process-death recovery or production security approval is claimed. No main merge.
