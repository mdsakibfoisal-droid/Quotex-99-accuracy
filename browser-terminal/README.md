# CB Market Assistant — Browser Demo

A mobile-first HTML/CSS/JavaScript demo with a floating/minimizable panel, illustrative candle-pattern scanner, expiry countdown, and a small rule-based market chat.

## Run it
Open `index.html` in a browser or serve this folder from a static host. No build step is required.

## What works in this demo
- Panel can be minimized to a floating bubble within this page and reopened.
- Sample scan rules illustrate engulfing, pin-bar, and three-candle patterns using hard-coded example candles.
- The expiry timer runs locally for 5, 15, 30, or 60 seconds.
- The market chat gives predefined educational responses.

## Important limits
This is **not connected to Quotex or any live market feed**. The market selector only changes the displayed label. Sample candles are synthetic examples, not real prices. The timer is not a broker trade, and it does not place orders. This demo does not measure profit or win rate. Chat replies are predefined and are not a live AI service. A bubble on this hosted page cannot float over a separate Quotex tab in Android Chrome.

Before any live-data integration, identify an authorized and reliable data source. Before any order automation, verify an authorized execution method and test it in demo mode. Never treat a simulated result as proof of profitability.