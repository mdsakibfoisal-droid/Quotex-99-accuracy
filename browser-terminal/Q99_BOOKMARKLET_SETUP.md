# Q99 Android bookmarklet — screenshot comparison prototype

## Current behavior
- Opens a Q99 panel on the current page.
- Lets you choose a chart screenshot and a reference-pattern image from your phone.
- Locally scans red/green pixel columns and compares the recent candidate shapes/order in both images.
- Shows an approximate visual similarity score and WAIT unless the score reaches the user-selected threshold.
- The score is experimental, not a probability of winning. It is not a validated UP/DOWN trading signal.
- Clicking the bookmark again closes the panel.

## Important limitations
- This does **not** read the live chart automatically or continuously.
- It does **not** reliably separate candle bodies from wicks, identify chart axes/timeframes, or distinguish candles from other red/green page elements. Grid lines, background art, zoom, and screenshot crop can distort the result.
- A high score only means the crude image heuristic found similar red/green shapes; it is not proof that the hand-drawn pattern matches.
- It does not submit trades, read account credentials, or connect to a live feed.
- OTC patterns may not behave the same across assets/platforms, and no pattern guarantees a result.

## Install on Android Chrome
1. Open the [bookmarklet source](https://github.com/mdsakibfoisal-droid/Quotex-99-accuracy/blob/feature/market-assistant-demo/browser-terminal/q99-bookmarklet.js).
2. Copy the complete one-line source beginning with `javascript:`.
3. In Chrome, create any bookmark and edit it.
4. Name it `Q99 Screenshot Test`; replace its URL/address with the copied source and save.
5. Open the Quotex page and tap that bookmark. If the browser strips or blocks `javascript:` URLs, it will not run in that browser/context.
6. In Q99, select a **cropped screenshot showing only the chart candles** and a reference drawing image. Tap `ছবি মিলিয়ে দেখুন`.
7. Treat the result as a visual experiment only. Do not place a trade based on the score.

## What is still needed for the intended version
1. The ZIP of hand-drawn patterns must be available and each image needs manual transcription/review (sequence, timeframe, market notes and marked direction).
2. The image algorithm must be validated against labelled screenshots, with candle body and upper/lower wick detected separately and false detections measured.
3. To generate live signals, a legitimate data source must supply matching symbol, market type (including OTC if available), timeframe and closed candles. A third-party feed may not match Quotex OTC data.
4. Add freshness/symbol/interval checks and fail closed to WAIT whenever the source or detection is uncertain.
5. Run repository tests and verify GitHub Actions. Test on Android Chrome; mobile browsers may block long bookmarklets.

Do not embed provider API keys in this bookmarklet or repository. No live feed is configured. Never mark unreviewed ZIP drawings as verified just to force a signal.
