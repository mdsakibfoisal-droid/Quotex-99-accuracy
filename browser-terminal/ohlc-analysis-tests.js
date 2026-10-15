/* Run with Node.js: node ohlc-analysis-tests.js
 * No packages required. These test supplied data only; they do not validate live-market accuracy.
 */
const assert = require("node:assert/strict");
const engine = require("./ohlc-analysis-engine.js");

const bullish = [
  { open: 10, high: 11, low: 9, close: 10.5 },
  { open: 10.5, high: 11.5, low: 10, close: 11 },
  { open: 11, high: 12, low: 10.8, close: 11.7 },
  { open: 11.7, high: 12.2, low: 11.4, close: 12 }
];
const bearish = [
  { open: 12, high: 12.5, low: 11.5, close: 11.8 },
  { open: 11.8, high: 12, low: 11, close: 11.3 },
  { open: 11.3, high: 11.5, low: 10.5, close: 10.8 },
  { open: 10.8, high: 11, low: 10, close: 10.2 }
];

assert.equal(engine.validateCandles(bullish).ok, true, "valid OHLC accepted");
assert.equal(engine.validateCandles([]).ok, false, "empty candles rejected");
assert.equal(engine.validateCandles(null).ok, false, "non-array input rejected");
assert.equal(engine.validateCandles(bullish.slice(0, 2)).ok, false, "fewer than three candles rejected");
assert.equal(engine.validateCandles([{ open: 5, high: 4, low: 3, close: 4 }]).ok, false, "invalid high rejected");
assert.equal(engine.validateCandles([{ open: 5, high: 6, low: 5.5, close: 4 }]).ok, false, "invalid low rejected");
assert.equal(engine.validateCandles([{ open: 5, high: NaN, low: 3, close: 4 }]).ok, false, "non-finite values rejected");
assert.equal(engine.analyze(bullish, { closed: false }).status, "WAIT", "unclosed candle must wait");
assert.equal(engine.analyze(bullish, { closed: true }).status, "CALL", "bullish sequence can be a CALL candidate");
assert.equal(engine.analyze(bearish, { closed: true }).status, "PUT", "bearish sequence can be a PUT candidate");

const bad = bullish.slice();
bad[3] = { open: 10, high: 8, low: 9, close: 11 };
assert.equal(engine.analyze(bad, { closed: true }).status, "WAIT", "invalid OHLC must wait");
const flat = [
  { open: 5, high: 5, low: 5, close: 5 },
  { open: 5, high: 5, low: 5, close: 5 },
  { open: 5, high: 5, low: 5, close: 5 }
];
assert.equal(engine.analyze(flat, { closed: true }).status, "WAIT", "flat/doji sequence should not signal");

const result = engine.analyze(bullish, { closed: true });
assert.equal(result.candleCount, 4, "candle count matches input");
assert.equal(result.lastCandle.direction, "bullish", "last candle direction calculated");
assert.equal(typeof result.disclaimer, "string", "analysis includes a no-guarantee disclaimer");
console.log("OHLC analysis tests passed.");
