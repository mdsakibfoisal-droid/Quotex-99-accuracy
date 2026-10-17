# Q99 Android bookmarklet — floating chart capability scan

## Current behavior
- Adds a small round Q99 button to the current page; no large panel.
- Tapping Q99 checks sizeable HTML canvases and whether a sampled pixel can be read from a 2D canvas.
- Distinguishes a missing 2D context (as on a canvas rendered with WebGL or another renderer) from a thrown pixel-access error.
- Shows WAIT when the chart cannot be read. A readable canvas is only a capability check, not candle detection.
- **It does not extract candle OHLC or produce UP/DOWN signals.** It deliberately fails closed until live candle extraction and reference labels are independently verified.
- Does not place trades, read cookies, call private endpoints, or connect to an external market-data provider.

## Install on Android Chrome
1. Open the [bookmarklet source](https://github.com/mdsakibfoisal-droid/Quotex-99-accuracy/blob/feature/market-assistant-demo/browser-terminal/q99-bookmarklet.js).
2. Copy the complete one-line source beginning with `javascript:`.
3. In Chrome, create any bookmark and edit it.
4. Name it `Q99 Signal`; replace the URL/address with the copied source and save.
5. Open the chart and tap that bookmark. If Chrome strips or blocks `javascript:` URLs, it will not run in that browser/context.
6. Tap the round Q99 button and read the diagnostic.

## Remaining work before any UP/DOWN signal
1. Find a permitted way to obtain the actual chart candle geometry. If the chart uses a protected/WebGL canvas, this bookmarklet cannot read its pixels; do not attempt to bypass browser security or use undocumented private endpoints.
2. Validate any extractor against screenshots at multiple zoom levels, themes, and scroll positions.
3. Transcribe all 98 hand-drawn reference images with candle count, left-to-right sequence, normalized body/wicks, and documented outcome. The manifest currently marks them unverified; no reference is allowed to trigger a signal yet.
4. Independently review transcriptions and establish whether each arrow means a predicted subsequent outcome or merely an entry annotation.
5. Connect verified live candle geometry to the strict reference matcher and show UP/DOWN only on a verified high-similarity match; otherwise WAIT.
6. Run automated tests and verify on Android Chrome.

## Current screenshot diagnosis
The reported result was one canvas and “pixel access blocked/unsupported (0)”. Since the error count was zero, the likely issue is that `getContext('2d')` returned null rather than `getImageData` throwing. The updated diagnostic reports that case separately as a non-2D renderer (possibly WebGL); it does not claim that WebGL was confirmed.

## Important limitations
A visual shape match does not guarantee the same future result. Similarity is not win probability. Do not trade based only on an unvalidated match. Never embed API keys in the bookmarklet or repository.
