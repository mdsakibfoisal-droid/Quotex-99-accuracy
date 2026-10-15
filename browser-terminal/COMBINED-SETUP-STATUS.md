# Combined market-assistant setup — implementation status

## Scope
This branch currently contains a browser-terminal demo and an Android project. The browser demo can show a floating/minimized panel and educational sample pattern rules.

## Important boundaries
- The browser demo's candle arrays are hard-coded examples. They are not Quotex candles.
- A timer finishing is not a win or loss. Do not count it as a trade outcome.
- No live Quotex feed, AI service, order placement, or verified win-rate engine is currently connected.
- Never display sample scans as live signals or simulated outcomes as actual broker results.

## Combined workflow target
1. **Data input:** use a documented, authorized market-data source or user-provided OHLC candle values. Mark the source and timestamp visibly. If data is missing/stale, return WAIT.
2. **Candle validation:** validate OHLC values (high >= open/close, low <= open/close, high >= low); analyze only closed candles.
3. **Signal engine:** detect engulfing, pin bar, and three-candle patterns; add trend/momentum/context checks only when sufficient real candles exist. Return CALL/PUT only when the configured checks agree; otherwise WAIT. Show reasons, candle timeframe, source, and timestamp.
4. **Demo validation:** record each signal, entry reference price, expiry, and the actual price at expiry from the same authorized feed. Derive win/loss only from those prices and the declared rule; otherwise mark UNVERIFIED. Store history locally and allow export/clear.
5. **Android overlay:** keep the draggable floating icon and panel separate from the signal engine. Screen capture alone does not guarantee reliable candle values; validate extraction against visible candles before enabling signals.
6. **Quality checks:** unit-test patterns and OHLC validation, test stale/missing data, run a historical backtest with costs/payout assumptions, and report sample size and uncertainty. No accuracy or profit guarantee.

## Current state
The browser-terminal scanner still uses illustrative sample candles. A live feed and validated chart-to-OHLC extraction are prerequisites before the live-analysis and outcome-validation stages can truthfully be enabled. The Android build and on-device capture are not verified by this document.
