import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import { validateDesign } from "../validate_design.mjs";
const board=JSON.parse(fs.readFileSync(new URL("../../data/boards/woodland_woven_trail.json", import.meta.url),"utf8"));
const catalogue=JSON.parse(fs.readFileSync(new URL("../../data/events/woven_events_v02.json", import.meta.url),"utf8"));
const clone=x=>structuredClone(x);

test("woven trail has 38 nodes, one start/finish, and five distinct crossroads",()=>{
  assert.equal(board.nodes.length,38);
  assert.equal(board.forks.length,5);
  assert.equal(validateDesign(board,catalogue).length,0);
});
test("all branch paths have a safe route to finish and no cycles",()=>{
  assert.deepEqual(validateDesign(board,catalogue),[]);
});
test("forks connect in a woven directed course rather than repeated diamond stages",()=>{
  const graph=new Map(board.nodes.map(n=>[n.id,n]));
  function shortest(id) {
    if(id===board.finish)return 0;
    return 1+Math.min(...graph.get(id).next.map(shortest));
  }
  assert.equal(board.forks.length,5);
  assert.ok(board.forks.some(f=>f.choices.length===3));
  for(const f of board.forks) {
    assert.equal(graph.get(f.id).kind,"fork");
    assert.deepEqual(f.choices.map(c=>c.to),graph.get(f.id).next);
    assert.equal(new Set(f.choices.map(c=>c.label)).size,f.choices.length);
    assert.ok(f.choices.every(c=>c.description && c.risk));
    if(f.choices.length===2){
      const high=f.choices.find(c=>c.risk==="high");
      const low=f.choices.find(c=>c.risk==="low");
      assert.ok(shortest(high.to)<shortest(low.to),"high-risk route should earn a shorter distance: "+f.id);
    }
  }
  assert.ok(graph.get("B1").next.includes("F3"),"bridge reconnects the upper junction to the lower crossroads");
});
test("validator rejects an accidental dead end",()=>{
  const b=clone(board);b.nodes.find(n=>n.id==="R2").next=[];
  assert.ok(validateDesign(b,catalogue).some(e=>/dead end|cannot safely/.test(e)));
});
test("validator rejects invalid event movement bounds",()=>{
  const c=clone(catalogue);c.events.find(e=>e.effect.type==="retreat").effect.spaces=99;
  assert.ok(validateDesign(board,c).some(e=>e.includes("out of bounds")));
});
test("validator rejects a missing fork target",()=>{
  const b=clone(board);b.nodes.find(n=>n.id==="F2").next[0]="MISSING";
  assert.ok(validateDesign(b,catalogue).some(e=>e.includes("missing edge target")));
});

test("controlled event pools keep high-risk and shelter encounters distinct",()=>{
  const spaces=board.nodes.filter(n=>n.kind==="event");
  for(const space of spaces) {
    const eligible=catalogue.events.filter(e=>e.regions.includes(space.region) && e.pools.includes(space.eventPool));
    assert.ok(eligible.length>=2,space.id+" insufficient event variety");
  }
  assert.ok(catalogue.events.some(e=>e.pools.includes("hazard") && e.effect.type==="retreat"));
  assert.equal(catalogue.events.filter(e=>e.pools.includes("shelter") && e.effect.type==="retreat").length,0);
});
test("validator catches dishonest advertised route risk",()=>{
  const b=clone(board);
  b.forks[0].choices[0].risk="low";
  assert.ok(validateDesign(b,catalogue).some(e=>e.includes("risk label")));
});
