const fs = require("node:fs");
const vm = require("node:vm");
const assert = require("node:assert/strict");

const source = fs.readFileSync("q99-bookmarklet.js", "utf8").trim();
assert.ok(source.startsWith("javascript:"), "bookmarklet must start with javascript:");
const script = source.slice("javascript:".length);
assert.ok(script.length > 100, "bookmarklet source must not be empty");
assert.doesNotThrow(() => new vm.Script(script), "bookmarklet must parse as JavaScript");
assert.match(script, /q99-bookmarklet-panel/, "bookmarklet should create its panel");
assert.match(script, /verified!==true/, "unverified references must be ignored");
assert.match(script, /\bWAIT\b/, "safe WAIT state must exist");
assert.doesNotMatch(script, /fetch\s*\(|XMLHttpRequest|\.submit\s*\(/, "prototype must not fetch external data or submit forms");
assert.doesNotMatch(script, /click\(\).*trade|placeTrade|buyNow|sellNow/i, "prototype must not implement trading actions");
console.log("Q99 bookmarklet syntax/safety checks passed");
