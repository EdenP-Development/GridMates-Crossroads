#!/usr/bin/env python3
"""Inspect an AI2 artifact ZIP without extracting files or third-party packages."""
import argparse
import csv
import json
from collections import Counter, defaultdict
from pathlib import PurePosixPath
from zipfile import ZipFile

KEYS = ("steady", "push", "supplies", "cover", "gamble", "brace")
FILES = ("gambler_competition.csv", "gambler_refinement.csv",
         "exact_strategy_calibration.csv", "guardian_competition.csv", "strategist_competition.csv", "scout_difficulty_competition.csv", "guardian_finish_competition.csv")


def summarize(archive):
    output = {}
    with ZipFile(archive) as z:
        for filename in FILES:
            matches = [n for n in z.namelist()
                       if PurePosixPath(n).name == filename]
            if not matches:
                continue
            if len(matches) != 1:
                raise ValueError("Ambiguous report: " + filename)
            with z.open(matches[0]) as f:
                rows = list(csv.DictReader(
                    line.decode("utf-8-sig") for line in f))
            if not rows:
                raise ValueError("Empty report: " + filename)
            grouped = defaultdict(Counter)
            matches_by_id = defaultdict(list)
            for row in rows:
                actor = row.get("policy", row.get("preset"))
                if not actor:
                    raise ValueError("Missing policy")
                seats = int(row["seats"])
                counts = {k: int(row[k]) for k in KEYS}
                if any(v < 0 for v in counts.values()):
                    raise ValueError("Negative counts")
                group = grouped[(actor, seats)]
                group.update(counts)
                group["records"] += 1
                if "won" in row:
                    won = int(row["won"])
                    if won not in (0, 1):
                        raise ValueError("Invalid winner flag")
                    group["wins"] += won
                    focal = row.get("gambler_seat", row.get("g2_seat", row.get("guardian_seat", row.get("strategist_seat", row.get("scout_seat")))))
                    key = (row["course"], row.get("opponent", "") + ":" + row.get("focal_policy", ""),
                           seats, row["seed"], focal)
                    matches_by_id[key].append((int(row["actor_seat"]), won))
            for key, records in matches_by_id.items():
                if sorted(s for s, _ in records) != list(range(1, key[2]+1)):
                    raise ValueError("Missing or duplicate seat: " + str(key))
                if sum(w for _, w in records) != 1:
                    raise ValueError("Invalid winner count: " + str(key))
            result = []
            for (actor, seats), counts in sorted(grouped.items()):
                def ratio(num, den):
                    return round(100*num/den, 4) if den else None
                result.append(dict(policy=actor, seats=seats, **counts,
                    push_pct=ratio(counts["push"], counts["push"]+counts["steady"]),
                    brace_pct=ratio(counts["brace"], counts["brace"]+counts["gamble"]),
                    win_pct=ratio(counts["wins"], counts["records"])
                    if matches_by_id else None))
            output[filename] = dict(rows=len(rows),
                matches=len(matches_by_id) if matches_by_id else len(rows),
                groups=result)
    if not output:
        raise ValueError("No recognized report in artifact")
    return output


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive")
    args = parser.parse_args()
    print(json.dumps(summarize(args.archive), indent=2))
