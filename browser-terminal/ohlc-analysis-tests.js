/* Run with Node.js: node ohlc-analysis-tests.js
 * No packages required. */
const assert = require("node:assert/strict");
const engine = require("./ohlc-analysis-engine.js");
const bullish = [
 {open:10,high:11,low:9,close:10.5},
 {open:10.5,high:11.5,low:10,close:11},
 {open:11,high:12,low:10.8,close:11.7},
 {open:11.7,high:12.2,low:11.4,close:12}
];
assert.equal(engine.validateCandles(bullish).ok, true, "valid OHLC accepted");
assert.equal(engine.validateCandles([]).ok, false, "empty candles rejected");
assert.equal(engine.validateCandles([{open:5,high:4,low:3,close:4}]).ok, false, "invalid high rejected");
assert.equal(engine.analyze(bullish,{closed:false}).status, "WAIT", "unclosed candle must wait");
assert.equal(engine.analyze(bullish,{closed:true}).status, "CALL", "bullish sequence can be a CALL candidate");
const bad = bullish.slice(); bad[3] = {open:10,high:8,low:9,close:11};
assert.equal(engine.analyze(bad,{closed:true}).status, "WAIT", "invalid OHLC must wait");
const result = engine.analyze(bullish,{closed:true});
assert.equal(result.candleCount, 4, "candle count matches input");
assert.equal(result.lastCandle.direction, "bullish", "last candle direction calculated");
console.log("OHLC analysis tests passed.");
