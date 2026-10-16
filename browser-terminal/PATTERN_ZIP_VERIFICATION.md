# Q99 hand-drawn pattern ZIP verification workflow

## Status
The manifest entries currently have `verified: false` and `transcription_status: pending`. They must remain disabled. Do not infer UP/DOWN outcomes from the shape alone.

## Review each image
1. Extract the ZIP without renaming files; preserve the original image and filename.
2. Create one manifest entry per image with a stable id and exact filename.
3. Inspect the original at full resolution. Record candle order left-to-right, color/direction, body, upper wick, lower wick, and any ambiguous/occluded marks.
4. Normalize each candle's body and wicks relative to its total high-low range. Ensure body + upper wick + lower wick is approximately 1 and direction is -1, 0, or 1.
5. Record the intended outcome only when the source image explicitly documents it or an independent labeling rule supports it. Otherwise set outcome to WAIT/UNVERIFIED and keep it disabled.
6. Have a second reviewer independently check the transcription and outcome. Record reviewer, date, source filename/hash, and notes.
7. Add automated fixture tests for each approved pattern and for ambiguous/incorrect inputs. Keep unverified patterns excluded from the active reference JSON.
8. Verify against held-out historical examples before using any result as a trading decision. A geometric match score is not a win probability.

## Required fields for an active reference
`id`, `label`, `source`, `verified: true`, `outcome: UP|DOWN|WAIT`, `candles`, and review evidence. Only use `verified: true` after the review is complete. Keep the source image in the audit set.


## Cross-OTC shape-matching requirement

The requested scope is to search for the same normalized candle geometry across OTC instruments. Instrument identity may vary, but pattern geometry must not be treated as proof of the same future outcome.

- Compare each candle's direction, normalized body, upper wick, and lower wick, plus left-to-right sequence.
- Normalize dimensions to each candle's own high-low range so price scale and quote level do not affect geometric matching.
- Use a strict threshold for the requested "near-identical" mode; return WAIT when any required candle is missing, chart pixels are unreadable, or the image transcription is ambiguous.
- Keep the pattern's intended UP/DOWN label separate from shape transcription. Do not activate a pattern unless its direction is explicitly readable and independently reviewed.
- A match means shape similarity only, not a guarantee of profitable OTC behavior. Different OTC instruments can generate similar shapes with different outcomes.
- The current Android Chrome diagnostic only inspects page elements; it does not yet extract candle geometry from the Quotex chart. Cross-OTC live matching is therefore not implemented until chart extraction and fixture validation pass.


## 2026-10-16 progress update

The newly uploaded archive `১৫ সেকেন্ড সাইকোলজি খাতায় আঁকানো(2).zip` was opened successfully. It contains 98 files (image files in the archive). A visual contact-sheet review confirms that the set mixes 3-candle and 4-candle drawings and includes handwritten UP/DOWN entry arrows, 15-second candle/trade timer notes, dates, and OTC annotations.

This is an inventory-level visual review only. It is not a full-resolution transcription of all 98 images. Individual candle geometry and the intended outcome for every image have not been independently verified, so no image has been promoted to an active signal reference. Existing `verified:false` manifest entries remain disabled.

## Live chart extraction status

The Android Chrome bookmarklet was run on the Quotex page. Its diagnostic found one canvas element (720×933 intrinsic size, approximately 424×549 CSS size) and 73 SVG elements. The earlier pixel-read test returned no readable pixel details. The current bookmarklet does not extract candle coordinates or shapes and cannot yet match the live chart to the archive. A DOM element count is not evidence that the candles were read.

Completion criteria before claiming live matching:
1. A repeatable extraction of candle bodies and upper/lower wicks from the visible chart, or an authorized structured OHLC feed that matches the OTC instrument.
2. Fixture tests comparing extracted candle geometry against manually reviewed reference images.
3. Independent review of each pattern's transcription and UP/DOWN label.
4. Fail-closed behavior: WAIT whenever chart extraction, timeframe, reference transcription, or match threshold is uncertain.

Do not report this feature as complete until those criteria pass.
