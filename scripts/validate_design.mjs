import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

/** Validate pre-production route/event *data*, not gameplay, UI, or Android LAN. */
export function validateDesign(board, catalogue) {
  const errors=[];
  const nodes=new Map();
  if (!Array.isArray(board.nodes) || !Array.isArray(catalogue.events)) return ["invalid top-level data"];
  for (const node of board.nodes) {
    if (nodes.has(node.id)) errors.push("duplicate node "+node.id);
    nodes.set(node.id,node);
  }
  if (!nodes.has(board.start) || !nodes.has(board.finish) || board.start===board.finish) errors.push("missing start or finish");
  if (board.nodes.filter(n=>n.kind==="start").length!==1 || board.nodes.filter(n=>n.kind==="finish").length!==1) errors.push("must have exactly one start and one finish");
  if (nodes.get(board.start)?.kind!=="start" || nodes.get(board.finish)?.kind!=="finish") errors.push("start/finish kinds mismatch");
  for (const node of board.nodes) {
    if (!Array.isArray(node.next)) {errors.push(node.id+": missing exits");continue;}
    if (node.id===board.finish && node.next.length) errors.push("finish has outbound edges");
    if (node.id!==board.finish && node.next.length===0) errors.push("unexpected dead end "+node.id);
    if (node.kind==="fork" && node.next.length<2) errors.push("fork without choices "+node.id);
    if (new Set(node.next).size!==node.next.length) errors.push("duplicate exits "+node.id);
    for (const next of node.next) if (!nodes.has(next)) errors.push("missing edge target "+node.id+" => "+next);
    if (!Number.isFinite(node.x) || !Number.isFinite(node.y) || node.x<0 || node.x>1 || node.y<0 || node.y>1) errors.push("bad drawing coordinates "+node.id);
  }
  const active=new Set(),visited=new Set(),memo=new Map();
  function reachesFinish(id) {
    if (!nodes.has(id)) return false;
    if (active.has(id)) {errors.push("route cycle at "+id);return false;}
    if (memo.has(id)) return memo.get(id);
    active.add(id);visited.add(id);
    const node=nodes.get(id);
    const ok=id===board.finish || node.next.length>0 && node.next.every(reachesFinish);
    active.delete(id);memo.set(id,ok);
    if (!ok) errors.push("route cannot safely reach finish from "+id);
    return ok;
  }
  reachesFinish(board.start);
  for (const node of board.nodes) if (!visited.has(node.id)) errors.push("unreachable node "+node.id);
  const regionIds=new Set((board.regions??[]).map(r=>r.id));
  for (const node of board.nodes) if (!regionIds.has(node.region)) errors.push("unknown region on node "+node.id);
  const forkIds=new Set();
  for (const fork of board.forks??[]) {
    if (forkIds.has(fork.id)) errors.push("duplicate fork "+fork.id);
    forkIds.add(fork.id);
    const node=nodes.get(fork.id);
    if (!node || node.kind!=="fork" || !Array.isArray(fork.choices) || fork.choices.length!==node.next.length) {errors.push("invalid fork definition "+fork.id);continue;}
    if (fork.choices.some(c=>!node.next.includes(c.to)||!c.label||!["low","medium","high"].includes(c.risk))) errors.push("invalid fork choice "+fork.id);
  }
  for (const node of board.nodes.filter(n=>n.kind==="fork")) if (!forkIds.has(node.id)) errors.push("undocumented fork "+node.id);
  const eventIds=new Set();
  for (const event of catalogue.events) {
    if (eventIds.has(event.id)) errors.push("duplicate event "+event.id);
    eventIds.add(event.id);
    if (!Array.isArray(event.regions) || !event.regions.length || event.regions.some(r=>!regionIds.has(r))) errors.push("invalid event region "+event.id);
    if (!Number.isInteger(event.weight) || event.weight<1) errors.push("invalid event weight "+event.id);
    const effect=event.effect??{};
    if (!["advance","retreat","shield","prank"].includes(effect.type)) errors.push("unsupported effect "+event.id);
    if (!Number.isInteger(effect.spaces) || effect.spaces<0 || effect.spaces>3 || (effect.type==="retreat" && effect.spaces>2)) errors.push("effect out of bounds "+event.id);
  }
  const pools=new Set(["all","hazard","shelter","mischief"]);
  for (const node of board.nodes) {
    const route=node.route??"neutral";
    if (!["neutral","risky","safe","mischief"].includes(route)) errors.push("invalid route classification on "+node.id);
    const pool=node.eventPool??"all";
    if (!pools.has(pool)) errors.push("invalid event pool on "+node.id);
  }
  for (const node of board.nodes.filter(n=>n.kind==="event")) {
    const pool=node.eventPool??"all";
    const eligible=catalogue.events.filter(e=>e.regions.includes(node.region)&&(e.pools??["all"]).includes(pool));
    if(eligible.length<1)errors.push("no events eligible for "+node.id);
    if(pool!=="all" && eligible.length<2)errors.push("too few events in pool for "+node.id);
  }
  for (const e of catalogue.events) {
    if((e.pools??["all"]).some(pool=>!pools.has(pool))) errors.push("unknown event pool "+e.id);
  }
  for(const fork of board.forks??[]) {
    const n=nodes.get(fork.id);
    if(!n)continue;
    for(const c of fork.choices??[]) {
      const target=nodes.get(c.to);
      if(!target)continue;
      const expected={"high":"risky","low":"safe","medium":"mischief"}[c.risk];
      if(target.route && target.route!=="neutral" && target.route!==expected)errors.push("risk label does not match route "+fork.id+" => "+c.to);
      if(!c.description)errors.push("missing route explanation "+fork.id+" => "+c.to);
    }
  }
  return [...new Set(errors)];
}

const invoked=process.argv[1] && path.resolve(process.argv[1])===fileURLToPath(import.meta.url);
if(invoked) {
  const catalogue=JSON.parse(fs.readFileSync(new URL("../data/events/woven_events_v02.json",import.meta.url),"utf8"));
  const boards=[
    "woodland_brookside_quick.json",
    "woodland_woven_trail.json",
    "woodland_cedarwood_long.json"
  ];
  for (const file of boards) {
    const board=JSON.parse(fs.readFileSync(new URL("../data/boards/"+file,import.meta.url),"utf8"));
    const errors=validateDesign(board,catalogue);
    if(errors.length) {
      console.error(file+":\n"+errors.join("\n"));
      process.exitCode=1;
    } else {
      console.log("Design data passed: "+file+" ("+board.nodes.length+
        " nodes, "+board.forks.length+" forks, "+catalogue.events.length+" events)");
    }
  }
}
