# Q99 validation log

## Automated matcher tests
- Date: 2026-10-16
- Runtime: Node.js v22.16.0
- Command: `node candle-pattern-matcher-tests.js`
- Result: PASS in a local isolated run using the matcher and test contents fetched from this branch.
- Coverage: valid/invalid OHLC bounds, normalized feature validation, exact similarity, verified-reference requirement, invalid outcomes, malformed references, mixed candle formats, too-short inputs, threshold clamping/default, and explicit WAIT outcomes.
- Note: this is a local test run; GitHub Actions status was not independently confirmed.

## Pattern ZIP
- Inventory: 98 JPEG images (plus the ZIP directory entry), no exact duplicate file hashes found in the preliminary inventory.
- All manifest entries remain `verified: false` and `transcription_status: pending`. No image-derived pattern is active.
- Reason: each image still needs full-resolution candle-by-candle transcription, outcome interpretation, and independent review. Hand-drawn direction labels or accuracy claims are not evidence of predictive performance.

## Live data
- Status: NOT CONNECTED.
- The browser demo does not read Quotex's protected chart and has no authorized live OHLC provider configured.
- Before live/replay integration, choose a provider/instrument and verify symbol, OTC status, timeframe, timestamps, latency, data rights, and candle-close behavior. Until that is complete, any absent or invalid data must remain WAIT.
- No API key should be committed to this repository or embedded in browser JavaScript.
