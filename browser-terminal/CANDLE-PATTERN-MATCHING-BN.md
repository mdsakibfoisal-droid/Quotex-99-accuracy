# Q99 pattern matcher — integration notes

This engine compares supplied OHLC candles with verified reference OHLC sequences. It does not itself read the Quotex chart, DOM, canvas, network traffic, or screenshot. Android Chrome bookmarklets cannot reliably access a protected/cross-origin chart canvas.

## Reference data format
Create a JSON array only after manually transcribing each reference image and verifying its candle sequence and labeled outcome:
```json
[{"id":"ref-001","label":"notebook page 1","source":"zip image filename","outcome":"UP","candles":[{"open":10,"high":12,"low":9,"close":11}]}]
```
Each entry needs at least two candles, and outcome must be UP, DOWN, or WAIT. Do not invent OHLC values from images; use WAIT until the reference has been accurately transcribed. A similarity score measures candle-shape similarity, not probability or expected profit.

## Next integration steps
1. Transcribe and review the ZIP's drawings into verified reference data.
2. Obtain live OHLC from a permitted, reliable data source or a documented platform interface. If unavailable, show "লাইভ ডেটা পাওয়া যায়নি" and do not emit a live signal.
3. Feed only closed candles to `Q99PatternMatcher.match(candles, references, {threshold: 90})`.
4. Keep all output advisory; no automated trade placement.
