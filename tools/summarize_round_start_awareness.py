#!/usr/bin/env python3
"""Validate paired round-start awareness experiment and summarize outcomes."""
import csv
import json
import sys
from collections import defaultdict, Counter

def summarize(path):
    with open(path,newline="",encoding="utf-8") as f:
        rows=list(csv.DictReader(f))
    pairs=defaultdict(dict)
    groups=defaultdict(Counter)
    for r in rows:
        course,seed,mode=r["course"],int(r["seed"]),r["mode"]
        if not 1<=seed<=64 or mode not in ("live","round_start") or mode in pairs[course,seed]:
            raise ValueError("Invalid pair")
        winner=int(r["winner"])
        rolls=[int(r[f"rolls_{i}"]) for i in (1,2)]
        push=[int(r[f"push_{i}"]) for i in (1,2)]
        changed=int(r["changed"])
        if winner not in (1,2) or rolls[0]!=rolls[1] or rolls[0]<1 or any(not 0<=p<=n for p,n in zip(push,rolls)) or not 0<=changed<=sum(rolls) or (mode=="live" and changed):
            raise ValueError("Invalid counts")
        pairs[course,seed][mode]=winner
        for key in ((mode,"all"),(mode,course)):
            c=groups[key]
            c["matches"]+=1
            c[f"wins_{winner}"]+=1
            c["changed"]+=changed
            for i in (1,2):
                c[f"rolls_{i}"]+=rolls[i-1]
                c[f"push_{i}"]+=push[i-1]
    courses={c for c,s in pairs}
    if len(courses)!=5 or set(pairs)!={(c,s) for c in courses for s in range(1,65)} or any(len(v)!=2 for v in pairs.values()) or len(rows)!=640:
        raise ValueError("Incomplete matrix")
    transitions=Counter((v["live"],v["round_start"]) for v in pairs.values())
    return dict(pairs=len(pairs),groups=[dict(mode=m,course=c,**dict(v)) for (m,c),v in sorted(groups.items())],
        transitions=[dict(live=a,round_start=b,count=n) for (a,b),n in sorted(transitions.items())])
if __name__=="__main__":
    print(json.dumps(summarize(sys.argv[1]),indent=2))
