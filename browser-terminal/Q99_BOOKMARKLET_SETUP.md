# Q99 Chrome bookmarklet — signal-only prototype

## What this version does
- Opens a small panel in the current page when the bookmarklet is run.
- Accepts manually supplied OHLC candles and a JSON array of reference patterns.
- Compares the latest candles with patterns marked `verified: true`.
- Displays UP or DOWN only when the geometric match score meets the chosen threshold; otherwise WAIT.
- Clicking the bookmark again closes the panel.

## What it does not do
- Does not capture the phone screen or read chart pixels.
- Does not read Quotex's protected chart or connect to a live feed.
- Does not submit trades or access account credentials.
- Does not prove that a pattern predicts the next candle. Match score is similarity, not win probability.

## Android Chrome installation (manual)
1. Open this file on GitHub and copy the entire single-line source beginning with `javascript:`: [q99-bookmarklet.js](https://github.com/mdsakibfoisal-droid/Quotex-99-accuracy/blob/feature/market-assistant-demo/browser-terminal/q99-bookmarklet.js).
2. In Chrome, create any bookmark, then edit that bookmark.
3. Set its name to `Q99 Signal` and replace its URL/address with the copied `javascript:...` source. Save.
4. On a normal webpage you control, open the bookmark from the bookmarks menu. If Chrome strips the `javascript:` URL or refuses to execute it, this method is not supported in that context; do not assume it works on the Quotex page.
5. The Q99 panel should appear in the current page. Tap the bookmark again to close it.

Mobile browsers may remove or block long bookmarklet URLs. This is a prototype, not an installed extension. Test on a harmless page first.

## Input shape
References must be a JSON array. Each active reference must have `verified: true`, `outcome: "UP"|"DOWN"|"WAIT"`, and at least two valid OHLC candles. Example only (synthetic data, not a real strategy):

```json
[
  {
    "id": "example-only",
    "label": "Synthetic example",
    "verified": true,
    "outcome": "UP",
    "candles": [
      {"open": 1, "high": 2, "low": 0.8, "close": 1.8},
      {"open": 1.8, "high": 2.4, "low": 1.5, "close": 2.2}
    ]
  }
]
```

Current candles use the same OHLC format. Values must be numeric and satisfy high >= open/close and low <= open/close. Reference examples must come from independent review before they are used; do not mark ZIP drawings verified just to force a signal.

## Remaining work before any live signal
1. Transcribe all relevant hand-drawn ZIP images at full resolution.
2. Record candle sequence, timeframe, OTC/regular market notes, and the author's marked direction separately from measured outcomes.
3. Get independent review and build historical/replay fixtures; keep ambiguous references disabled.
4. Select an authorized feed that actually matches the asset, OTC status, and timeframe. A third-party feed may not match Quotex OTC candles.
5. Add timestamp, symbol, interval, closed-candle, and stale-data checks to any future feed adapter; failure must always clear the signal to WAIT.
6. Run the repository's Node tests and confirm the GitHub Actions workflow passes.
7. Test the bookmarklet on Android Chrome and document where it is blocked or stripped.

Never embed provider API keys in the bookmarklet or repository. No live feed is configured in this prototype.
