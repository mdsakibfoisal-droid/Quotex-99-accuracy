# Md Jibon Quotex 99% - Complete Android Trading Automation App

## 🎯 App Overview

A complete Android trading automation app for Quotex with:
- Real-time chart pattern recognition
- Floating action icon with countdown timer
- 96-pattern database with UP/DOWN signal display
- Auto BUY/SELL trade execution
- Trade history and statistics
- Full automation workflow

## 🚀 Features

### Core Features
✅ **Floating Icon System**
- Draggable floating icon (Q99)
- Real-time 15-second candle countdown timer
- UP/DOWN signal display with color coding:
  - 🔼 UP (Green) = BUY signal
  - 🔽 DOWN (Red) = SELL signal
  - 🔵 Q99 (Blue) = Idle

✅ **Pattern Recognition Engine**
- 96 stored candle patterns
- Real-time pattern matching from live chart
- Format: `Color-BodySize-UpperWick-LowerWick` (R/G-L/M/S-L/M/S-L/M/S)
- Exact match required for trade execution

✅ **Trading Automation**
- 15-second candle cycle
- Valid trade window: Last 5 seconds (10-15s)
- Automatic BUY/SELL execution on pattern match
- Trade logging with timestamp and result

✅ **User Interface**
- Main dashboard with trade statistics
- Settings screen (trade amount, scan interval, notifications)
- Trade history with win rate tracking
- Real-time status updates

✅ **Advanced Features**
- Accessibility service integration for auto clicks
- Notifications and vibration alerts
- Database storage for trade records
- Pattern extraction from screenshots

## 📋 Installation & Setup

### Requirements
- Android 8.0+ (API 26)
- Quotex app installed
- Python 3.7+ (for pattern extraction)

### Build Instructions

```bash
# Clone repository
git clone https://github.com/mdsakibfoisal-droid/Quotex-99-accuracy.git
cd Quotex-99-accuracy

# Build APK
./gradlew assembleRelease
```

## 🔧 Configuration

### Pattern Database
The app includes 96 pre-built candle patterns. To update:

1. Take 96 chart screenshots named: `01_EURUSD_BUY.png` to `96_...png`
2. Run pattern extractor:
   ```bash
   python3 extract_patterns.py screenshots_folder
   ```
3. Update `PatternDatabase.kt` with extracted patterns

### Settings
In-app settings for:
- Trade Amount (1-1000)
- Scan Interval (0.5-5 seconds)
- Enable/Disable Notifications
- Enable/Disable Vibration
- Auto Trade Execution

## 🎮 Usage

### Basic Flow
1. Open Quotex and select a market (EURUSD, GBPUSD, etc.)
2. Launch this app
3. Grant required permissions:
   - Overlay (SYSTEM_ALERT_WINDOW)
   - Accessibility Service
   - Notifications
4. Tap the floating Q99 icon to start scanning
5. Icon will show:
   - Countdown timer (MM:SS)
   - UP/DOWN signal when pattern matches
   - Green highlight in last 5 seconds of candle

### Floating Icon Behavior
- **Q99** (Blue): Not scanning
- **RUN** (Light Blue): Scanning but no match
- **🔼 UP** (Green): BUY signal detected
- **🔽 DOWN** (Red): SELL signal detected
- **Countdown**: Shows time remaining in current 15-second candle

## 📊 Trading Logic

### Pattern Matching
```
Live Chart → Extract Candles → Convert to Format
     ↓
"G-L-S-S, R-M-M-S" → Compare with Database
     ↓
Exact Match Found → Check Time Window
     ↓
Invalid (0-10s) → Wait for next candle
Valid (10-15s) → EXECUTE TRADE ✅
```

### Candle Format
- **Color**: R (RED) or G (GREEN)
- **Body Size**: L (LARGE), M (MEDIUM), S (SMALL)
- **Upper Wick**: L, M, or S
- **Lower Wick**: L, M, or S

Example: `R-L-S-S` = Red candle, Large body, Small upper wick, Small lower wick

## 📱 Permissions Required

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

## 🔐 Accessibility Service Setup

1. Go to Settings → Accessibility
2. Find "Md Jibon Quotex 99%"
3. Enable the service
4. Grant all requested permissions

This allows the app to automatically click BUY/SELL buttons.

## 📈 Trade History

View all executed trades:
- Timestamp of execution
- Market and action (BUY/SELL)
- Pattern ID used
- Match confidence score
- Trade status
- Win rate statistics

## 🐛 Troubleshooting

**Icon not showing?**
- Check overlay permission in Settings
- Restart the app

**Pattern not matching?**
- Ensure chart has at least 2 visible candles
- Check pattern database is loaded
- Verify candle color detection is working

**Trades not executing?**
- Enable Accessibility Service
- Check Quotex is in foreground
- Verify valid trading window (last 5 seconds)

## 📚 Development

### Project Structure
```
app/
├── src/main/
│   ├── java/com/mdjibon/quotex99/
│   │   ├── MainActivity.kt
│   │   ├── SettingsActivity.kt
│   │   ├── TradeHistoryActivity.kt
│   │   ├── accessibility/
│   │   ├── database/
│   │   ├── engine/
│   │   ├── service/
│   │   └── utils/
│   └── res/
│       ├── layout/
│       ├── values/
│       └── xml/
├── build.gradle.kts
└── AndroidManifest.xml
```

### Key Classes
- `FloatingIconService`: Manages floating icon and countdown
- `PatternDatabase`: Stores 96 patterns
- `AdvancedPatternMatcher`: Matches live patterns
- `CandleAnalyzer`: Extracts candle data
- `TimingEngine`: Manages 15-second candle cycles
- `TradeExecutorService`: Logs trades to database
- `QuotexAccessibilityService`: Auto-clicks BUY/SELL

## 📝 License

Apache License 2.0

## 👨‍💻 Author

Md Sakib Foisal

## 🤝 Contributing

Contributions welcome! Please submit pull requests.

## ⚠️ Disclaimer

This is an educational project. Use at your own risk. Trading involves risk of loss.
