const assert=require("node:assert/strict");
const {match,shape}=require("./q99-reference-matcher.js");
const candles=[
 {open:10,high:12,low:9,close:11},
 {open:11,high:14,low:10,close:13}
];
assert.equal(shape(candles[0]).direction,"UP");
const ref={id:"reviewed-example",verified:true,interval:"15s",marketType:"OTC",signal:"UP",candles};
const payload={interval:"15s",marketType:"OTC",candles};
assert.equal(match(payload,[ref],{minScore:98}).status,"MATCH");
assert.equal(match({...payload,interval:"1m"},[ref]).status,"WAIT");
assert.equal(match({...payload,marketType:"REGULAR"},[ref]).status,"WAIT");
assert.equal(match(payload,[{...ref,verified:false}]).status,"WAIT");
assert.equal(match(payload,[{...ref,signal:"MAYBE"}]).status,"WAIT");
assert.equal(match({...payload,candles:[{open:1,high:1,low:1,close:1}]},[ref]).status,"WAIT");
assert.equal(match(payload,[],{}).status,"WAIT");
const changed={...payload,candles:[{open:10,high:14,low:9,close:9.1},candles[1]]};
assert.notEqual(match(changed,[ref],{minScore:98}).status,"MATCH");
console.log("Q99 reference matcher tests passed");
