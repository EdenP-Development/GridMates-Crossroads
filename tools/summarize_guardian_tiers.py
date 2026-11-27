#!/usr/bin/env python3
"""Validate Guardian difficulty matrix and print reproducible focal results."""
import csv
import json
import sys
from collections import defaultdict, Counter

POLICIES = ("GUARDIAN_RELAXED_CANDIDATE_V1", "GUARDIAN_V1",
            "GUARDIAN_FINISH_CANDIDATE_V1")
COUNTS = ("steady", "push", "low", "medium", "high", "supplies",
          "cover", "gamble", "brace", "target", "target_pass")

def summarize(path):
    with open(path, newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    matches = defaultdict(list)
    for r in rows:
        n = int(r["seats"])
        focal = int(r["guardian_seat"])
        seed = int(r["seed"])
        p, q = r["focal_policy"], r["opponent"]
        if n not in (2, 4) or not 1 <= focal <= n or not 1 <= seed <= 24:
            raise ValueError("Invalid match dimensions")
        if p not in POLICIES or q not in POLICIES or p == q:
            raise ValueError("Invalid policy pair")
        if n == 2 and POLICIES.index(p) >= POLICIES.index(q):
            raise ValueError("Duplicate reversed two-seat pair")
        matches[(r["course"], p, q, n, seed, focal)].append(r)
    courses = sorted({k[0] for k in matches})
    expected = {(c,p,q,n,s,f) for c in courses for p in POLICIES
                for q in POLICIES if p != q for n in (2,4)
                if n == 4 or POLICIES.index(p) < POLICIES.index(q)
                for s in range(1,25) for f in range(1,n+1)}
    if len(courses)!=5 or set(matches)!=expected or len(rows)!=12960:
        raise ValueError("Incomplete or unexpected tournament matrix")
    groups = defaultdict(Counter)
    by_course = defaultdict(Counter)
    for (course,p,q,n,seed,focal), actors in matches.items():
        if sorted(int(r["actor_seat"]) for r in actors)!=list(range(1,n+1)):
            raise ValueError("Invalid actor seats")
        if any(r["won"] not in ("0","1") for r in actors) or sum(int(r["won"]) for r in actors)!=1:
            raise ValueError("Invalid winner flags")
        action_counts={int(r["match_actions"]) for r in actors}
        if len(action_counts)!=1 or sum(int(r[k]) for r in actors for k in COUNTS)!=next(iter(action_counts)):
            raise ValueError("Action counts disagree")
        for r in actors:
            is_focal=int(r["actor_seat"])==focal
            if r["policy"]!=(p if is_focal else q):
                raise ValueError("Policy does not match seat assignment")
            if any(int(r[k])<0 for k in COUNTS):
                raise ValueError("Negative count")
            if not is_focal and n==4:
                continue
            actor, opponent=(p,q) if is_focal else (q,p)
            for group in (groups[(actor,opponent,n)],by_course[(course,actor,opponent,n)]):
                group["matches"]+=1
                group["wins"]+=int(r["won"])
                group.update({k:int(r[k]) for k in COUNTS})
    def record(key,c):
        return dict(policy=key[-3],opponent=key[-2],seats=key[-1],
                    **({"course":key[0]} if len(key)==4 else {}),
                    **dict(c),win_pct=round(100*c["wins"]/c["matches"],4))
    return dict(actor_rows=len(rows),matches=len(matches),
                pair_results=[record(k,c) for k,c in sorted(groups.items())],
                course_results=[record(k,c) for k,c in sorted(by_course.items())])

if __name__=="__main__":
    print(json.dumps(summarize(sys.argv[1]),indent=2))
