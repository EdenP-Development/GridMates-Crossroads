#!/usr/bin/env python3
"""Validate homogeneous seat audit and report first-arrival and winner shares."""
import csv
import json
import sys
from collections import Counter, defaultdict

POLICIES = {"LEGACY_V1","ADAPTIVE_V2_R1","ADAPTIVE_V2_R2","GAMBLER_V1","GUARDIAN_V1","STRATEGIST_V1"}

def summarize(path):
    with open(path, newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    keys = set()
    groups = defaultdict(Counter)
    for r in rows:
        n, seed = int(r["seats"]), int(r["seed"])
        first, winner = int(r["first_seat"]), int(r["winner_seat"])
        key = (r["course"],r["policy"],n,seed)
        if key in keys or n not in (2,3,4) or not 1 <= seed <= 64 or r["policy"] not in POLICIES:
            raise ValueError("Invalid or duplicate match")
        keys.add(key)
        if not (1 <= first <= n and 1 <= winner <= n and 1 <= int(r["finishers"]) <= n):
            raise ValueError("Invalid finish receipt")
        if r["tied_best"] not in ("true","false"):
            raise ValueError("Invalid tie flag")
        rolls = [int(r[f"rolls_{i}"]) for i in range(1,n+1)]
        if min(rolls) < 1 or sum(rolls) > int(r["actions"]):
            raise ValueError("Invalid roll counts")
        if any(r[f"rolls_{i}"] for i in range(n+1,5)):
            raise ValueError("Inactive seat rolls")
        for group in ((n,"all","all"),(n,r["policy"],"all"),(n,"all",r["course"])):
            c = groups[group]
            c["matches"] += 1
            c[f"first_{first}"] += 1
            c[f"winner_{winner}"] += 1
            c["changed_winner"] += first != winner
            c["tied_best"] += r["tied_best"] == "true"
            for i, count in enumerate(rolls,1):
                c[f"rolls_{i}"] += count
    courses = {k[0] for k in keys}
    expected = {(c,p,n,s) for c in courses for p in POLICIES for n in (2,3,4) for s in range(1,65)}
    if len(courses)!=5 or keys!=expected or len(rows)!=5760:
        raise ValueError("Incomplete matrix")
    results = []
    for (n,policy,course), c in sorted(groups.items()):
        results.append(dict(seats=n,policy=policy,course=course,matches=c["matches"],
            first=[c[f"first_{i}"] for i in range(1,n+1)],
            wins=[c[f"winner_{i}"] for i in range(1,n+1)],
            rolls=[c[f"rolls_{i}"] for i in range(1,n+1)],
            changed_winner=c["changed_winner"],tied_best=c["tied_best"]))
    return dict(matches=len(rows),groups=results)

if __name__ == "__main__":
    print(json.dumps(summarize(sys.argv[1]),indent=2))
