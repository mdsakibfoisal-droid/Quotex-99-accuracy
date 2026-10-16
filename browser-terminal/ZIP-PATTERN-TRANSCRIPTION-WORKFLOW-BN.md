# ZIP Pattern Transcription — Work Queue

The supplied ZIP contains 98 JPG images. A contact-sheet scan confirms hand-drawn candle diagrams and annotations, but low-resolution thumbnails are insufficient to reliably transcribe every candle and handwritten UP/DOWN outcome. Therefore no pattern is automatically activated from the images.

## Workflow
1. Review each original JPG at full resolution.
2. Record each candle in sequence as relative shape features or numeric OHLC if the price scale is legible.
3. Record the written entry direction and the exact candle/expiry it refers to.
4. Mark a row verified only after a second review.
5. Keep unclear, contradictory, or incomplete examples as WAIT and do not train/use them.
6. Validate references on separate historical examples before showing any advisory match.

## Data schema
```json
{
  "id": "img-001-pattern-01",
  "sourceImage": "original JPG filename",
  "verified": false,
  "candleCount": 3,
  "candles": [
    {"open": 0, "high": 1, "low": -1, "close": 0.7}
  ],
  "outcome": "WAIT",
  "outcomeMeaning": "The image's exact label and target candle have not been verified",
  "notes": ""
}
```

Do not copy example OHLC values above into production data; they are schema placeholders only. When the drawn chart has no readable price scale, use a documented normalized shape representation instead of inventing market prices. The current matcher requires valid OHLC arrays; it must be extended before normalized image shapes can be compared.

## Important
A pattern that looks the same is not proof that the next candle will move the same way. The source archive's claims must be evaluated as hypotheses, not guaranteed signals. The matcher must return WAIT if no verified reference exists or live candle data is unavailable.

## Live chart
The standalone matcher currently receives candles from manual JSON. No authorized live Quotex OHLC feed or documented page interface has been confirmed. Do not claim the Android Chrome page can see protected canvas pixels or hidden chart data. Integrate only a documented, permitted data source; otherwise display "লাইভ ডেটা পাওয়া যায়নি".
