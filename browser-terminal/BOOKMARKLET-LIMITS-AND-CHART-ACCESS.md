# Chrome bookmarklet: chart access and realistic capabilities

## Scope
This workflow intentionally uses Chrome bookmarks and a script running inside the currently open webpage. It does not require a separate Android app or APK.

## What a bookmarklet can do
- Add a draggable floating button/panel inside the current page.
- Read normal DOM text and visible elements that the page exposes to scripts.
- Use browser speech recognition and speech synthesis when Chrome/device permissions and language support allow.
- Analyze OHLC values supplied by the user or from a documented data API that is authorized for this use.
- Capture errors and clearly say when data is not available.

## What it cannot reliably do by itself
- Put an overlay on top of all Chrome tabs, the Android system UI, or other apps.
- Read the private JavaScript state of a broker's chart when it is isolated in a cross-origin iframe or protected canvas.
- Convert any canvas/SVG element count into a candle count.
- Infer precise OHLC values or true order flow from element presence alone.
- Place trades safely unless a supported, authorized broker interface exists and the user explicitly confirms each order.

## Recommended implementation path
1. Run the overlay only on the actual chart tab, after the page is loaded.
2. Detect same-origin frames and visible chart canvases; test canvas pixel access defensively and catch SecurityError. Never treat a readable canvas as proof that pixel-based candle extraction is accurate.
3. For screenshot interpretation, require explicit user capture/upload or use a browser-supported visible-tab capture flow where supported; show permission prompts and never silently capture unrelated pages.
4. Develop a separate candle-image detector against labeled screenshots and return confidence per candle. If confidence is low, show “unable to read” instead of a signal.
5. Prefer an authorized OHLC feed or user-imported OHLC CSV/JSON for numerical analysis.
6. Test on Android Chrome for bookmarklet execution, speech recognition, Bangla voice availability, layout, drag behavior, zoom and screen rotation.

## Trading and accuracy
A pattern-based CALL/PUT is a probabilistic hypothesis, not a guarantee. Buyer/seller pressure from candle shapes is only a proxy; real order flow needs order-book/trade data. Do not automate orders through page-click simulation or bypass broker protections. No accuracy or profitability claim should be made without a reproducible backtest and forward test.
