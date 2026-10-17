# Q99 Android bookmarklet — floating chart capability scan

## Current behavior
- Adds a small round Q99 button to the current page; no large panel.
- Tapping Q99 checks whether the page exposes sizeable HTML canvas elements and whether their pixels can be read.
- Shows a short diagnostic toast: canvas found/readable, or blocked/not found.
- **It does not yet extract candle OHLC or produce UP/DOWN signals.** It deliberately returns WAIT until candle extraction and the reference library are verified.
- Does not place trades, read cookies, call private endpoints, or connect to an external market-data provider.

## Install on Android Chrome
1. Open the [bookmarklet source](https://github.com/mdsakibfoisal-droid/Quotex-99-accuracy/blob/feature/market-assistant-demo/browser-terminal/q99-bookmarklet.js).
2. Copy the complete one-line source beginning with `javascript:`.
3. In Chrome, create any bookmark and edit it.
4. Name it `Q99 Signal`; replace the URL/address with the copied source and save.
5. Open the Quotex chart and tap that bookmark. If Chrome strips or blocks `javascript:` URLs, it will not run in that browser/context.
6. Tap the round Q99 button. Read the diagnostic message. A readable canvas is only a first capability check—not proof that candle geometry can be extracted correctly.

## What remains before signals
1. Verify the detected canvas is the actual candle plot, not another page canvas.
2. Extract candle bodies/wicks from the visible chart and validate against screenshots at multiple zoom levels, themes, and scroll positions.
3. Transcribe all 98 hand-drawn reference images with candle count, left-to-right sequence, normalized body/wicks, and documented outcome. The manifest currently marks them unverified; no reference is allowed to trigger a signal yet.
4. Independently review transcriptions and establish that each UP/DOWN annotation means the subsequent outcome, not just the arrow's direction.
5. Connect the chart extractor to the strict reference matcher and show UP/DOWN only on a verified high-similarity match; otherwise WAIT.
6. Run tests and verify on Android Chrome.

## Important limitations
Canvas access can be blocked or the chart may be rendered using WebGL or a protected layer. The current diagnostic does not bypass browser security. A visual shape match does not guarantee the same future result; do not trade based only on an unvalidated match. Never embed API keys in the bookmarklet or repository.
