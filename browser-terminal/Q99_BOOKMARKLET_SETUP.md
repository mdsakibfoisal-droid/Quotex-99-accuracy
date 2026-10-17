# Q99 Android bookmarklet — minimal floating button

## Current behavior
- Creates a small round Q99 floating button on the current page.
- Tapping the button shows a small `WAIT` toast; tapping again dismisses it.
- Does not open a large panel.
- Does not read the live Quotex chart, compare the 98 reference images, or issue validated UP/DOWN signals.
- Does not place trades or connect to an external data feed.

## Install on Android Chrome
1. Open the [bookmarklet source](https://github.com/mdsakibfoisal-droid/Quotex-99-accuracy/blob/feature/market-assistant-demo/browser-terminal/q99-bookmarklet.js).
2. Copy the complete one-line source beginning with `javascript:`.
3. In Chrome, create any bookmark and edit it.
4. Name it `Q99 Signal`; replace the URL/address with the copied source and save.
5. Open the Quotex page and tap that bookmark. If Chrome strips or blocks `javascript:` URLs, it will not run in that browser/context.

## Work remaining before UP/DOWN can be shown responsibly
1. Transcribe each hand-drawn image with candle count, left-to-right direction, normalized body, upper wick, lower wick, timeframe notes, and any marked outcome.
2. Independently review each transcription. All current manifest entries remain `verified: false` and must not be used as signal references.
3. Implement a permitted live chart extraction method or authorized OHLC feed that actually matches the OTC instrument and 15-second timeframe.
4. Validate candle extraction against manually labeled screenshots, including chart themes, zoom levels, wick/body separation, and false positives.
5. Compare the live candle sequence with reviewed references and show UP/DOWN only if the matching reference's outcome is independently supported; otherwise show WAIT.
6. Run automated tests and verify on Android Chrome. A similarity percentage is not a win probability.

## Important limitations
A third-party OHLC feed may not match Quotex OTC prices. A visual shape match does not guarantee the same future result. Do not trade based only on an unvalidated match. Do not embed API keys in the bookmarklet or repository.
