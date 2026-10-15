# Remaining work status — Android Chrome draggable Quotex helper

## Completed in the repository
- Added `ANDROID-CHROME-DRAGGABLE-BOOKMARKLET.txt` with a single-line JavaScript bookmarklet and Android Chrome setup steps.
- The bookmarklet creates a floating Q99 bubble on the current webpage, supports touch dragging, opens/closes a compact Bengali panel, and gives a limited chart-element check.
- It is designed for the existing Quotex tab and does not require an APK.

## Not verified yet
- The bookmarklet has not been run on the user's physical Android phone or tested against the current Quotex website. Browser bookmarklet execution may be blocked by some Chrome versions.
- The bubble's presence and movement are the only intended core features; the chart scan detects possible chart elements, not actual candles.

## Not possible to honestly mark complete with this bookmarklet alone
- Reading protected/cross-origin chart data or reliably deriving OHLC from the chart's canvas.
- Accurate live candle counts/colors/wicks, live CALL/PUT predictions, or measured prediction accuracy.
- Overlaying the bubble over other tabs/apps; webpage scripts are confined to their page.
- Automated trade placement. No trade automation is included.

## User test checklist
1. Save the entire `javascript:` line from `ANDROID-CHROME-DRAGGABLE-BOOKMARKLET.txt` as a Chrome bookmark URL.
2. Open the Quotex chart, then invoke the bookmark from Chrome's address bar bookmark suggestion.
3. Confirm the Q99 bubble appears and can be dragged to four screen corners; tap to open/close panel.
4. If nothing appears, check whether Chrome removed the `javascript:` prefix or blocked bookmarklet execution.

Do not describe this as a live chart-reading assistant until live chart data access and device testing are actually verified.
