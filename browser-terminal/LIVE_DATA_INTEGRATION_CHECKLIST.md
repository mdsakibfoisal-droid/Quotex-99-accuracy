# Q99 live candle data integration

## Status
The bookmarklet currently accepts manually supplied OHLC JSON. It does not read Quotex chart pixels, private page state, cookies, or undocumented internal APIs. Do not describe it as a live scanner until a supported feed has been integrated and tested.

## Integration contract
Implement a provider adapter only for a data source whose terms and permissions explicitly allow the intended use. Provider output must be normalized to:
```json
{
  "symbol": "EXAMPLE",
  "timeframe": "15s",
  "candles": [
    {"time": 0, "open": 1.0, "high": 1.2, "low": 0.9, "close": 1.1}
  ],
  "source": "provider-name",
  "receivedAt": "ISO-8601 timestamp"
}
```
The `time` value should be a provider-defined epoch timestamp (seconds or milliseconds, documented by the adapter). Never mix units silently.

## Required safety and quality gates
1. Document the provider, licensing/terms, symbol mapping, timeframe support, timestamp unit, rate limits, and whether OTC symbols are actually supported.
2. Reject missing, stale, duplicate, out-of-order, malformed, or impossible OHLC candles.
3. Check candle interval continuity where the provider guarantees regular intervals; explicitly flag gaps rather than fabricate candles.
4. Keep live data collection separate from pattern scoring. The matcher returns WAIT when data or verified patterns are absent.
5. Never use cookies, session tokens, private browser state, undocumented Quotex APIs, or automated trade-clicking.
6. Keep a manual OHLC JSON mode for testing and fallback.
7. Backtest on historical/replay data with costs/payout assumptions and report sample size, wins/losses, and uncertainty. Shape similarity is not a probability of success.
8. Do not mark handwritten ZIP patterns verified until each transcription and its outcome have been independently reviewed.

## Implementation order
- [ ] Select an authorized provider and confirm exact instrument/timeframe coverage.
- [ ] Implement provider-specific adapter outside the bookmarklet, if permitted.
- [ ] Add schema, freshness, order, and continuity checks with tests.
- [ ] Add replay/backtest tooling and a no-lookahead test.
- [ ] Test Android Chrome manually with the live provider disabled first, then with the authorized feed.
- [ ] Only then consider showing a clearly labeled live-data status in the UI.

## Why there is no live adapter yet
No authorized live data provider or API credentials have been configured in this repository. A placeholder endpoint would be misleading and would not make the scanner work. Quotex OTC prices may not match third-party feeds.
