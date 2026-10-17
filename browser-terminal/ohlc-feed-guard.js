/* Q99 live-data boundary: validate provider-supplied closed OHLC candles.
 * This module does not fetch data or read Quotex's protected chart.
 */
(function(root){
"use strict";
function finite(n){return typeof n==="number"&&Number.isFinite(n)}
function validCandle(c){
  return !!c&&finite(c.open)&&finite(c.high)&&finite(c.low)&&finite(c.close)&&
    c.high>=Math.max(c.open,c.close)&&c.low<=Math.min(c.open,c.close)&&c.high>=c.low;
}
function validatePayload(payload,expected,nowMs){
  expected=expected||{};nowMs=finite(nowMs)?nowMs:Date.now();
  const fail=(reason)=>({ok:false,status:"WAIT",reason,candles:[]});
  if(!payload||typeof payload!=="object")return fail("ডেটা প্যাকেট নেই।");
  if(payload.symbol!==expected.symbol)return fail("সিম্বল মেলেনি।");
  if(payload.interval!==expected.interval)return fail("টাইমফ্রেম মেলেনি।");
  if(payload.marketType!==expected.marketType)return fail("মার্কেট টাইপ মেলেনি (OTC/নন-OTC)।");
  if(!Array.isArray(payload.candles)||payload.candles.length<2)return fail("কমপক্ষে ২টি ক্যান্ডেল দরকার।");
  const maxAgeMs=finite(expected.maxAgeMs)&&expected.maxAgeMs>=0?expected.maxAgeMs:15000;
  const seen=new Set();let previous=-Infinity;
  for(const c of payload.candles){
    if(!validCandle(c))return fail("অবৈধ OHLC মান।");
    if(!finite(c.timestamp)||c.timestamp>nowMs+1000)return fail("ক্যান্ডেলের টাইমস্ট্যাম্প অবৈধ বা ভবিষ্যতের।");
    if(seen.has(c.timestamp)||c.timestamp<=previous)return fail("ক্যান্ডেল ডুপ্লিকেট বা ভুল ক্রমে আছে।");
    if(c.closed!==true)return fail("অসমাপ্ত ক্যান্ডেল বাদ দেওয়া হয়েছে।");
    seen.add(c.timestamp);previous=c.timestamp;
  }
  const latest=payload.candles[payload.candles.length-1];
  if(nowMs-latest.timestamp>maxAgeMs)return fail("ডেটা পুরোনো; নতুন ডেটা না আসা পর্যন্ত WAIT।");
  return {ok:true,status:"READY",reason:"ডেটা ফরম্যাট ও সতেজতা যাচাই হয়েছে; এটি প্যাটার্নের নির্ভুলতা যাচাই নয়।",candles:payload.candles};
}
const api={validCandle,validatePayload};if(typeof module!=="undefined"&&module.exports)module.exports=api;root.Q99OHLCGuard=api;
})(typeof globalThis!=="undefined"?globalThis:this);
