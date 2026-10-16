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
