# Candle geometry extraction: implementation plan and acceptance tests

## Goal
Compare visible Quotex OTC candle shapes against manually reviewed reference patterns. A shape match is not a prediction guarantee. The feature must emit WAIT unless extraction, timeframe, reference transcription, and match threshold are all valid.

## Current observed limitation
The Android Chrome diagnostic on `market-qx.info/en/trade` reported one canvas (720x933 intrinsic, about 424x549 CSS) and 73 SVG nodes. This does not prove the candle chart is readable. The current bookmarklet does not extract candle bodies/wicks. Browser JavaScript cannot read cross-origin iframe internals or tainted canvas pixels, and a bookmarklet cannot silently capture the whole phone screen.

## Safe extraction routes (try in this order)
1. **Authorized structured OHLC feed**: only if the provider explicitly supports the same OTC instrument and interval. Validate symbol, interval, timestamps, closed-candle status, OHLC consistency and freshness using `ohlc-feed-guard.js`. Do not scrape private endpoints, cookies, session tokens, or undocumented platform APIs.
2. **User-selected chart screenshot**: allow the user to choose a screenshot file from the phone. Analyze visible candle colors and geometry from that image, after the user confirms chart plot bounds and candle interval. This is screenshot matching, not continuous live monitoring.
3. **Visible DOM/canvas pixels**: probe only elements in the current page. For each canvas, test 2D/WebGL availability and pixel-read access. Do not claim success until detected candle centers and OHLC geometry match several manually annotated screenshots. If cross-origin/security restrictions block pixels, return WAIT.

## Geometry representation
For each candle with image-space coordinates:
- body bounds: x center, body top/bottom
- wick top and bottom
- direction: up/down/doji, calibrated against chart colors
- normalized values: body-height/range, upper-wick/range, lower-wick/range
- horizontal gap normalized by median candle spacing
Compare the ordered sequence of the exact same candle count. Require all candles to satisfy per-feature tolerances; a single broad average score must not disguise a mismatched candle. The chart's price scale and screenshot resolution should not affect normalized geometry.

## Reference transcription status
The ZIP inventory contains 98 image files with mixed 3-candle and 4-candle drawings and handwritten arrows/notes. Inventory is not transcription. Every reference stays disabled until a reviewer records candle geometry, timeframe/context if stated, intended UP/DOWN label, and review status. Ambiguous labels remain disabled.

## Required test fixtures before enabling signals
- At least 10 manually annotated chart screenshots spanning green/red candles and visible upper/lower wicks.
- Exact same pattern should match after screenshot resizing/cropping that preserves plot geometry.
- Reversed candle order, changed candle color, and materially changed wick/body ratio must fail strict matching.
- Occluded candles, low resolution, uncertain chart bounds, stale feed, wrong interval, and unverified reference must return WAIT.
- Compare extracted coordinates to human annotations and record false-positive/false-negative counts.
- Never describe shape similarity as win probability; never place trades automatically.

## Current status
NOT IMPLEMENTED. The current bookmarklet only reports page element counts. No live candle extraction or verified reference library is enabled. Do not mark this feature complete until fixture tests pass on the target Android Chrome/Quotex page.
