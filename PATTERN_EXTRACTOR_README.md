# Quotex Pattern Extractor

এই Python script স্বয়ংক্রিয়ভাবে আপনার 96টি screenshot থেকে candle data বের করে দেবে।

## ⚙️ Installation

```bash
pip install opencv-python numpy
```

## 📖 ব্যবহার করার নিয়ম

### Step 1: ফোল্ডার তৈরি করুন
```bash
mkdir patterns_screenshots
```

### Step 2: সব 96টি screenshot এখানে রাখুন
```
patterns_screenshots/
├── 01_EURUSD_BUY.png
├── 02_EURUSD_SELL.png
├── 03_GBPUSD_BUY.png
└── ... (96টি সব)
```

### Step 3: Script চালান

```bash
python3 extract_patterns.py patterns_screenshots
```

অথবা output format নির্বাচন করুন:

```bash
# শুধু JSON
python3 extract_patterns.py patterns_screenshots --format json

# শুধু CSV
python3 extract_patterns.py patterns_screenshots --format csv

# শুধু Kotlin (Android এর জন্য)
python3 extract_patterns.py patterns_screenshots --format kotlin

# সব (default)
python3 extract_patterns.py patterns_screenshots --format all
```

## 📤 Output Files

Script এই files তৈরি করবে:

1. **patterns_data.json** - সব candle data JSON format-এ
2. **patterns_data.csv** - Excel/Sheets এ খোলার জন্য
3. **patterns_data.kt** - Android app-এ ব্যবহার করার জন্য

## 📋 Output Format উদাহরণ

### JSON
```json
[
  {
    "id": 1,
    "market": "EURUSD",
    "action": "BUY",
    "candle1": {
      "color": "GREEN",
      "bodySize": "MEDIUM",
      "upperWick": "MEDIUM",
      "lowerWick": "SMALL"
    },
    "candle2": {
      "color": "RED",
      "bodySize": "SMALL",
      "upperWick": "SMALL",
      "lowerWick": "MEDIUM"
    },
    "trend": "BULLISH_REVERSAL",
    "confidence": 85
  }
]
```

### CSV
```
id,market,action,c1_color,c1_body,c1_upper_wick,c1_lower_wick,c2_color,c2_body,c2_upper_wick,c2_lower_wick,trend,confidence
1,EURUSD,BUY,GREEN,MEDIUM,MEDIUM,SMALL,RED,SMALL,SMALL,MEDIUM,BULLISH_REVERSAL,85
```

## 🔷 Kotlin Output
আপনার Android app-এ directly use করতে পারবেন।

## ✨ Features

✅ Automatic candle color detection (GREEN/RED)
✅ Body size classification (SMALL/MEDIUM/LARGE)
✅ Wick size measurement
✅ Trend detection (BULLISH/BEARISH/REVERSAL)
✅ Batch processing (সব 96টি এক সাথে)
✅ Multiple output formats
✅ Progress indicator

## 🆘 Troubleshooting

**Image format issue?**
- নিশ্চিত করুন ফাইলের নাম format: `ID_MARKET_ACTION.png`
- PNG ছাড়াও JPG, BMP support করে

**Color detection not working?**
- Screenshot quality check করুন
- Lighting/brightness adjust করুন

## 📞 Help

কোন error দেখলে console output দেখুন - সেখানে details থাকবে।
