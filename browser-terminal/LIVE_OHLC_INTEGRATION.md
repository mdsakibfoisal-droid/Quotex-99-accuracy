# Q99 live OHLC integration plan (safe, fail-closed)

## Current verified state
The browser-terminal demo accepts manually supplied OHLC or normalized candle shapes. It does not read Quotex's protected chart and does not currently consume any live feed. Do not label manual input as a live signal.

## Recommended first integration
1. Select a licensed market-data provider and verify that the selected symbol, asset class (including OTC vs non-OTC), timeframe, latency, and redistribution/use rights fit the intended use.
2. For the prototype, use historical OHLC from the provider's documented time-series endpoint. For a later live phase, use a provider-supported stream or a small server-side adapter that turns ticks into timestamped OHLC candles. A price-only WebSocket is not itself an OHLC candle stream.
3. Keep provider credentials on a server-side adapter or protected secret store. Never commit an API key into this repository or expose a paid key in client-side JavaScript.
4. The adapter must normalize and validate numeric OHLC, candle timestamps, symbol, interval, market session, and data freshness. Reject malformed, stale, out-of-order, duplicated, or incomplete candles.
5. Keep the last closed candle separate from the currently forming candle. Do not match until the selected reference and input use the same candle interval and market type.
6. On disconnect, quota/rate limit, stale data, missing candle, symbol mismatch, or provider error, set status to WAIT and clear any prior signal. Never reuse a previous UP/DOWN result as if it were current.
7. Add provider contract/terms review and verify that the feed is appropriate for the actual Quotex instrument. A third-party forex price is not guaranteed to match a broker's OTC or broker-specific chart.
8. Test in historical/replay mode first. Compare sample candles and timestamps manually against the chosen data provider before considering a live display.

## Candidate provider to evaluate
Twelve Data documents HTTP time-series OHLC and WebSocket price streaming. Its WebSocket FAQ says the stream is price events, not OHLC candles, so OHLC aggregation would need to be built and tested separately; availability and plan limits must be checked for the account. Docs:
- https://twelvedata.com/docs
- https://support.twelvedata.com/en/articles/5620516-how-to-stream-the-data
- https://support.twelvedata.com/en/articles/5194610-websocket-faq

## Browser and same-page limitation
A panel hosted as a normal webpage cannot overlay or inspect another tab in Android Chrome. A same-page integration only works if Q99 is actually loaded in that same page and the chart exposes data through an authorized interface. Do not scrape private chart internals, bypass platform protections, or automate trades. If the chart exposes no authorized OHLC interface, the realistic route is an external licensed feed plus an explicit symbol/timeframe selection, with the mismatch caveat above.

## Acceptance gates
- Provider authorization and data rights confirmed.
- No API secrets in client bundle/repository.
- Freshness and symbol/timeframe checks tested.
- Invalid, stale, disconnected, and unverified reference cases always yield WAIT.
- Historical replay fixtures and automated tests pass.
- No claim of prediction accuracy without a separate out-of-sample evaluation.
