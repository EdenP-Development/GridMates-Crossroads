#!/usr/bin/env python3
"""Summarize observed travel decisions; never infer causal effects from them."""
import csv
import json
import sys
from collections import defaultdict, Counter

def summarize(path):
    with open(path,newline="",encoding="utf-8") as f:
        rows=list(csv.DictReader(f))
    groups=defaultdict(Counter)
    matches=defaultdict(Counter)
    seen=set()
    for r in rows:
        key=(r["course"],r["policy"],int(r["seed"]))
        seat=int(r["seat"])
        action=int(r["action"])
        if seat not in (1,2) or not 1<=key[2]<=64 or action<0 or (key,action) in seen:
            raise ValueError("Invalid or duplicate decision")
        seen.add((key,action))
        if r["push"] not in ("true","false") or r["final_round"] not in ("true","false"):
            raise ValueError("Invalid boolean")
        gap=int(r["gap"])
        if int(r["distance"])<0 or r["stage"] not in ("OPENING","MIDGAME","ENDGAME"):
            raise ValueError("Invalid race state")
        matches[key][seat]+=1
        c=groups[(r["policy"],seat)]
        c["rolls"]+=1
        c["push"]+=r["push"]=="true"
        c["trailing"]+=gap>0
        c["leading"]+=gap<0
        c["tied"]+=gap==0
        c["final_round"]+=r["final_round"]=="true"
        c["opening"]+=r["stage"]=="OPENING"
        c["endgame"]+=r["stage"]=="ENDGAME"
        if r["finish_distance"]:
            c["direct_finish"]+=1
            steady,push=int(r["steady_successes"]),int(r["push_successes"])
            if not (0<=steady<=6 and 0<=push<=6):
                raise ValueError("Invalid odds")
            expected="" if steady==push else str(push>steady).lower()
            if r["recommended_push"]!=expected:
                raise ValueError("Invalid recommendation")
            if expected:
                c["unequal_direct_odds"]+=1
                c["lower_direct_odds_choice"]+=r["push"]!=expected
    policies={"LEGACY_V1","ADAPTIVE_V2_R1","ADAPTIVE_V2_R2","GAMBLER_V1","GUARDIAN_V1","STRATEGIST_V1"}
    courses={k[0] for k in matches}
    expected={(c,p,s) for c in courses for p in policies for s in range(1,65)}
    if len(courses)!=5 or set(matches)!=expected or any(c[1]!=c[2] or c[1]<1 for c in matches.values()):
        raise ValueError("Incomplete or unequal-turn match matrix")
    return dict(matches=len(matches),rolls=len(rows),groups=[
        dict(policy=p,seat=s,**dict(c)) for (p,s),c in sorted(groups.items())])

if __name__=="__main__":
    print(json.dumps(summarize(sys.argv[1]),indent=2))
