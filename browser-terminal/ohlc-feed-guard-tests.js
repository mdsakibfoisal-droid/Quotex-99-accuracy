const assert=require("node:assert/strict");
const g=require("./ohlc-feed-guard.js");
const now=100000;
const base={symbol:"EUR/USD",interval:"15s",marketType:"OTC",candles:[
 {timestamp:85000,open:1,high:1.2,low:.9,close:1.1,closed:true},
 {timestamp:95000,open:1.1,high:1.3,low:1,close:1.2,closed:true}
]};
const expected={symbol:"EUR/USD",interval:"15s",marketType:"OTC",maxAgeMs:10000};
assert.equal(g.validatePayload(base,expected,now).status,"READY");
assert.equal(g.validatePayload({...base,symbol:"GBP/USD"},expected,now).status,"WAIT");
assert.equal(g.validatePayload({...base,interval:"1m"},expected,now).status,"WAIT");
assert.equal(g.validatePayload({...base,marketType:"REGULAR"},expected,now).status,"WAIT");
assert.equal(g.validatePayload({...base,candles:[...base.candles,{...base.candles[1]}]},expected,now).status,"WAIT");
assert.equal(g.validatePayload({...base,candles:[base.candles[0],{...base.candles[1],closed:false}]},expected,now).status,"WAIT");
assert.equal(g.validatePayload({...base,candles:[base.candles[0],{...base.candles[1],high:1}]},expected,now).status,"WAIT");
assert.equal(g.validatePayload({...base,candles:[base.candles[0],{...base.candles[1],timestamp:1}]},expected,now).status,"WAIT");
assert.equal(g.validatePayload({...base,candles:[base.candles[0],{...base.candles[1],timestamp:now+5000}]},expected,now).status,"WAIT");
assert.equal(g.validatePayload({...base,candles:[base.candles[0],{...base.candles[1],timestamp:70000}]},expected,now).status,"WAIT");
console.log("Q99 OHLC feed guard tests passed");