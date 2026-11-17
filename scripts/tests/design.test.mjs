import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import { validateDesign } from "../validate_design.mjs";
const board=JSON.parse(fs.readFileSync(new URL("../../data/boards/woodland_quick_example.json", import.meta.url),"utf8"));
const catalogue=JSON.parse(fs.readFileSync(new URL("../../data/events/core_events_proposed.json", import.meta.url),"utf8"));
const clone=x=>structuredClone(x);

test("sample has 40 nodes, one start/finish, and three documented forks",()=>{
  assert.equal(board.nodes.length,40);
  assert.equal(board.forks.length,3);
  assert.equal(validateDesign(board,catalogue).length,0);
});
test("all branch paths have a safe route to finish and no cycles",()=>{
  assert.deepEqual(validateDesign(board,catalogue),[]);
});
test("each fork offers a shorter and a longer route",()=>{
  for(const f of board.forks) {
    const stage=Number(f.id.slice(1));
    const merge="M"+stage;
    const lengths=f.choices.map(choice=>{
      let id=choice.to,steps=0;
      while(id!==merge && steps<100){
        steps++;const n=board.nodes.find(n=>n.id===id);
        assert.ok(n && n.next.length===1);
        id=n.next[0];
      }
      assert.equal(id,merge);return steps;
    });
    assert.notEqual(lengths[0],lengths[1]);
  }
});
test("validator rejects an accidental dead end",()=>{
  const b=clone(board);b.nodes.find(n=>n.id==="U1_2").next=[];
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
