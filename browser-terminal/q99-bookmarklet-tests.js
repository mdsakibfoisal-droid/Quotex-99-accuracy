const fs = require("node:fs");
const vm = require("node:vm");
const assert = require("node:assert/strict");

const source = fs.readFileSync("q99-bookmarklet.js", "utf8").trim();
assert.ok(source.startsWith("javascript:"), "bookmarklet must start with javascript:");
const script = source.slice("javascript:".length);
assert.ok(script.length > 100, "bookmarklet source must not be empty");
assert.doesNotThrow(() => new vm.Script(script), "bookmarklet must parse as JavaScript");
assert.match(script, /q99-floating-button/, "bookmarklet should create a floating button");
assert.match(script, /border-radius:50%/, "floating button should be round");
assert.match(script, /width:48px;height:48px/, "floating button should be compact");
assert.match(script, /q99-signal-toast/, "button should show a small result toast");
assert.match(script, /x\.textContent='WAIT'/, "unavailable live matching must fail closed to WAIT");
assert.match(script, /লাইভ ক্যান্ডেল শনাক্তকরণ এখনো সংযুক্ত নয়/, "must disclose missing live candle detection");
assert.doesNotMatch(script, /q99-floating-panel|<section|<textarea|<input type="file"/, "minimal version must not open a large panel");
assert.doesNotMatch(script, /fetch\s*\(|XMLHttpRequest|\.submit\s*\(/, "prototype must not fetch external data or submit forms");
assert.doesNotMatch(script, /placeTrade|buyNow|sellNow/i, "prototype must not implement trading actions");
console.log("Q99 minimal floating-button syntax/safety checks passed");
