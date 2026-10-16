const assert=require("node:assert/strict");
const fs=require("node:fs");
const path=require("node:path");
const manifest=JSON.parse(fs.readFileSync(path.join(__dirname,"pattern-image-manifest.json"),"utf8"));
assert.equal(manifest.length,98,"manifest must list all 98 ZIP JPGs");
assert.equal(new Set(manifest.map(x=>x.file)).size,98,"manifest filenames must be unique");
for(const item of manifest){
 assert.equal(item.verified,false,"ZIP images must remain unverified until manually reviewed");
 assert.equal(item.outcome,"UNVERIFIED");
 assert.equal(item.transcription_status,"pending");
}
const matcher=require("./candle-pattern-matcher.js");
const candles=[{open:1,high:2,low:.5,close:1.8},{open:1.8,high:2.4,low:1.6,close:2.3},{open:2.3,high:2.8,low:2.1,close:2.7}];
const unverified=[{id:"not-reviewed",candles,outcome:"UP",verified:false}];
assert.equal(matcher.match(candles,unverified).status,"WAIT","unverified pattern must never emit a signal");
console.log("Q99 ZIP manifest safety tests passed (98 pending images; unverified patterns disabled)");
