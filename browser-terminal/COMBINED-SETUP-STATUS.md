# Combined market-assistant setup — implementation status

## Added in this branch
- `voice-market-assistant.html`: Bengali speech input/output where supported, typed Q&A, user-supplied OHLC JSON input, analysis display and result JSON copy.
- `ohlc-analysis-engine.js`: validates OHLC ranges, describes the last candle's direction/body/wicks, checks basic engulfing/pin-bar/three-candle patterns, and returns WAIT when OHLC is invalid or the latest candle is not confirmed closed.
- `ohlc-analysis-tests.js`: dependency-free Node.js regression checks for basic valid/invalid OHLC and closed-candle behavior.
- Android project already includes a floating overlay and user-approved screen capture code, but its image extraction remains experimental and is not validated against the live Quotex chart.

## What is not complete
- No authorized live Quotex OHLC feed is connected.
- The browser voice page does not automatically read chart pixels or the broker's private chart data. Users must provide OHLC values.
- Screen capture does not yet produce validated OHLC values from the actual chart. Candidate pixel detection is not reliable candle recognition.
- No live AI service is connected; speech is browser speech recognition/synthesis plus limited local Q&A.
- No broker order placement is enabled. Do not treat timer completion as a win/loss.
- No historical backtest, measured accuracy, or profitability claim has been established.

## Correct next steps
1. Integrate a documented, authorized data source or a validated manual/import workflow; label source, timeframe, and timestamp.
2. Improve candle detection against recorded screenshots and ground-truth labels. If confidence is insufficient, return WAIT.
3. Add a real local history log and export/clear actions using actual entry and expiry prices from the same trusted source; otherwise mark results UNVERIFIED.
4. Run the regression tests and Android build, then test on a physical Android device and the actual chart before claiming support.
5. Add historical backtesting with payout/cost assumptions, sample size, and uncertainty.

## Safety and accuracy
Buyer/seller pressure inferred from candle shapes is only a proxy, not actual order-flow data. Candle patterns cannot guarantee direction or profit. Never show sample or unverified screen detections as live signals.
