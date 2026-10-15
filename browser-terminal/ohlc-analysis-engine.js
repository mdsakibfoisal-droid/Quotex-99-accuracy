/* Quotex Market Assistant — validated OHLC analysis core
 * This module analyzes supplied OHLC candles only. It does not fetch Quotex data.
 * Candle shape: {time, open, high, low, close}; newest candle must be CLOSED.
 * Use in browser via <script src="ohlc-analysis-engine.js"></script>.
 */
(function (root) {
  "use strict";
  const finite = n => typeof n === "number" && Number.isFinite(n);
  function validateCandles(candles) {
    if (!Array.isArray(candles) || candles.length < 3) {
      return { ok: false, error: "কমপক্ষে ৩টি সম্পূর্ণ ক্যান্ডেলের OHLC ডেটা দিন।" };
    }
    for (let i = 0; i < candles.length; i++) {
      const c = candles[i];
      if (!c || !["open", "high", "low", "close"].every(k => finite(c[k]))) {
        return { ok: false, error: (i + 1) + " নম্বর ক্যান্ডেলের OHLC ডেটা অসম্পূর্ণ।" };
      }
      if (c.high < Math.max(c.open, c.close) ||
          c.low > Math.min(c.open, c.close) || c.high < c.low) {
        return { ok: false, error: (i + 1) + " নম্বর ক্যান্ডেলের OHLC মান অবৈধ।" };
      }
    }
    return { ok: true };
  }
  function metrics(c) {
    const range = c.high - c.low;
    const body = Math.abs(c.close - c.open);
    return {
      direction: c.close > c.open ? "bullish" : c.close < c.open ? "bearish" : "doji",
      body,
      range,
      upperWick: c.high - Math.max(c.open, c.close),
      lowerWick: Math.min(c.open, c.close) - c.low,
      bodyPercent: range ? body / range * 100 : 0
    };
  }
  function analyze(candles, options) {
    options = options || {};
    const valid = validateCandles(candles);
    if (!valid.ok) return { status: "WAIT", valid: false, reason: valid.error };
    if (options.closed !== true) {
      return { status: "WAIT", valid: true, reason: "সর্বশেষ ক্যান্ডেল বন্ধ হয়েছে কি না নিশ্চিত করুন; চলমান ক্যান্ডেলে প্যাটার্ন বদলাতে পারে।" };
    }
    const n = candles.length, a = candles[n - 1], b = candles[n - 2], d = candles[n - 3];
    const ma = metrics(a), mb = metrics(b), md = metrics(d);
    const bodyA = Math.abs(a.close - a.open), bodyB = Math.abs(b.close - b.open);
    const engulfBull = mb.direction === "bearish" && ma.direction === "bullish" &&
      a.open <= b.close && a.close >= b.open;
    const engulfBear = mb.direction === "bullish" && ma.direction === "bearish" &&
      a.open >= b.close && a.close <= b.open;
    const pinBull = ma.range > 0 && ma.lowerWick / ma.range >= 0.55 && ma.bodyPercent <= 35;
    const pinBear = ma.range > 0 && ma.upperWick / ma.range >= 0.55 && ma.bodyPercent <= 35;
    const threeBull = md.direction === "bullish" && mb.direction === "bullish" && ma.direction === "bullish";
    const threeBear = md.direction === "bearish" && mb.direction === "bearish" && ma.direction === "bearish";
    const last = candles.slice(-5);
    const bullishCount = last.filter(c => c.close > c.open).length;
    const bearishCount = last.filter(c => c.close < c.open).length;
    const trend = bullishCount >= 4 ? "স্বল্পমেয়াদি ঊর্ধ্বমুখী প্রবণতার ইঙ্গিত" :
      bearishCount >= 4 ? "স্বল্পমেয়াদি নিম্নমুখী প্রবণতার ইঙ্গিত" : "স্পষ্ট স্বল্পমেয়াদি প্রবণতা নেই";
    const patterns = [];
    if (engulfBull) patterns.push("Bullish engulfing");
    if (engulfBear) patterns.push("Bearish engulfing");
    if (pinBull) patterns.push("সম্ভাব্য bullish pin bar");
    if (pinBear) patterns.push("সম্ভাব্য bearish pin bar");
    if (threeBull) patterns.push("তিনটি bullish candle");
    if (threeBear) patterns.push("তিনটি bearish candle");
    let status = "WAIT", reason = "পরীক্ষিত প্যাটার্নে যথেষ্ট মিল নেই।";
    if (engulfBull || (pinBull && bullishCount >= 3) || (threeBull && bullishCount >= 4)) {
      status = "CALL"; reason = "কয়েকটি bullish শর্ত মিলেছে; এটি নিশ্চয়তা নয়।";
    } else if (engulfBear || (pinBear && bearishCount >= 3) || (threeBear && bearishCount >= 4)) {
      status = "PUT"; reason = "কয়েকটি bearish শর্ত মিলেছে; এটি নিশ্চয়তা নয়।";
    }
    return {
      status, valid: true, reason, patterns, trend,
      candleCount: candles.length,
      lastCandle: {
        direction: ma.direction,
        body: ma.body, upperWick: ma.upperWick, lowerWick: ma.lowerWick,
        bodyPercent: Math.round(ma.bodyPercent * 10) / 10
      },
      pressure: ma.direction === "bullish" ? "শেষ ক্যান্ডেলে ক্রেতাদের পক্ষে মূল্য-চলন দেখা যায়" :
        ma.direction === "bearish" ? "শেষ ক্যান্ডেলে বিক্রেতাদের পক্ষে মূল্য-চলন দেখা যায়" :
        "শেষ ক্যান্ডেলে দিক স্পষ্ট নয়",
      disclaimer: "এটি সরবরাহ করা OHLC ডেটার নিয়মভিত্তিক ব্যাখ্যা, নিশ্চিত ভবিষ্যদ্বাণী নয়। ডেটার উৎস/সময় যাচাই করুন; কোনো লাভের নিশ্চয়তা নেই।"
    };
  }
  root.QuotexOHLCAnalysis = { validateCandles, metrics, analyze };
})(typeof window !== "undefined" ? window : globalThis);
