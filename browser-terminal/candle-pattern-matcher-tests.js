const assert=require("node:assert/strict");
const m=require("./candle-pattern-matcher.js");
const up=[
 {open:1,high:2,low:.5,close:1.8},
 {open:1.8,high:2.4,low:1.6,close:2.3},
 {open:2.3,high:2.8,low:2.1,close:2.7}
];
const refs=[{id:"test-up",label:"test",verified:true,candles:up,outcome:"UP"}];
assert.equal(m.valid(up),true,"valid OHLC is accepted");
assert.equal(m.valid([{open:2,high:1,low:0,close:1}]),false,"invalid OHLC bounds are rejected");
const normalized=[
 {dir:1,body:.6,upper:.1,lower:.3},
 {dir:-1,body:.7,upper:.2,lower:.1}
];
assert.equal(m.validShape(normalized),true);
assert.equal(m.similarity(normalized,normalized),100);
assert.equal(m.validShape([{dir:1,body:2,upper:0,lower:0}]),false);
assert.equal(m.similarity(up,up),100);
assert.equal(m.match(up,refs,{threshold:95}).signal,"UP");
assert.equal(m.match(up,[],{}).status,"WAIT","empty reference set fails closed");
assert.equal(m.match(up,[{...refs[0],verified:false}],{}).status,"WAIT","unverified refs are ignored");
assert.equal(m.match(up,[{...refs[0],outcome:"BUY"}],{}).status,"WAIT","unsupported outcomes are ignored");
assert.equal(m.match(up,[{...refs[0],candles:[{open:2,high:1,low:0,close:1}]}],{}).status,"WAIT","malformed reference is ignored");
assert.equal(m.match([{dir:1,body:.5,upper:.2,lower:.3},{open:1,high:2,low:0,close:1}],refs).status,"WAIT","mixed formats are rejected");
assert.equal(m.match([{open:1,high:2,low:.5,close:1.8}],refs).status,"WAIT","too few candles do not match a 3-candle ref");
assert.equal(m.match(up,refs,{threshold:101}).signal,"UP","threshold is clamped to 100; exact match remains eligible");
assert.equal(m.match(up,refs,{threshold:-100}).signal,"UP","threshold is clamped to 1");
assert.equal(m.match(up,refs,{threshold:1000}).signal,"UP","threshold is clamped to 100");
assert.equal(m.match(up,refs,{threshold:NaN}).signal,"UP","non-finite threshold uses default");
assert.equal(m.match(up,[{...refs[0],outcome:"WAIT"}],{}).status,"WAIT","WAIT outcome never emits signal");
console.log("Q99 pattern matcher tests passed");