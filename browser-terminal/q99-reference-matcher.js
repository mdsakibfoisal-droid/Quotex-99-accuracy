/* Q99 reference matcher.
 * Compares normalized candle geometry only. Requires independently reviewed
 * references and closed OHLC input from an authorized, matching data source.
 * A similarity score is not a probability of winning.
 */
(function(root){
"use strict";
function valid(c){
 return !!c&&["open","high","low","close"].every(k=>typeof c[k]==="number"&&Number.isFinite(c[k]))&&
 c.high>=Math.max(c.open,c.close)&&c.low<=Math.min(c.open,c.close)&&c.high>=c.low;
}
function shape(c){
 const range=c.high-c.low;
 if(!(range>0))return null;
 return {
  direction:c.close>c.open?"UP":c.close<c.open?"DOWN":"DOJI",
  body:Math.abs(c.close-c.open)/range,
  upper:(c.high-Math.max(c.open,c.close))/range,
  lower:(Math.min(c.open,c.close)-c.low)/range
 };
}
function match(payload,references,opts){
 opts=opts||{};
 const wait=(reason)=>({status:"WAIT",reason,score:null,referenceId:null,signal:null});
 if(!payload||payload.interval!=="15s"||payload.marketType!=="OTC")return wait("শুধু যাচাইকৃত ১৫ সেকেন্ড OTC ডেটা গ্রহণযোগ্য।");
 if(!Array.isArray(payload.candles)||payload.candles.length<2||!payload.candles.every(valid))return wait("লাইভ OHLC ক্যান্ডেল অবৈধ বা অপর্যাপ্ত।");
 if(!Array.isArray(references))return wait("যাচাইকৃত রেফারেন্স নেই।");
 const live=payload.candles.map(shape);
 if(live.some(x=>!x))return wait("শূন্য-রেঞ্জ ক্যান্ডেল; নিরাপদে মিলানো যায়নি।");
 const minScore=Number.isFinite(opts.minScore)?Math.max(1,Math.min(100,opts.minScore)):98;
 let best=null;
 for(const r of references){
  if(!r||r.verified!==true||r.interval!=="15s"||r.marketType!=="OTC"||
     !["UP","DOWN"].includes(r.signal)||!Array.isArray(r.candles)||r.candles.length<2||
     r.candles.length>live.length||!r.candles.every(valid))continue;
  const ref=r.candles.map(shape);if(ref.some(x=>!x))continue;
  const a=live.slice(-ref.length);let error=0;
  for(let i=0;i<ref.length;i++){
   if(a[i].direction!==ref[i].direction)error+=1;
   error+=Math.abs(a[i].body-ref[i].body);
   error+=Math.abs(a[i].upper-ref[i].upper);
   error+=Math.abs(a[i].lower-ref[i].lower);
  }
  const score=Math.round(Math.max(0,100*(1-error/(ref.length*4))));
  if(!best||score>best.score)best={status:"MATCH",score,referenceId:r.id||null,signal:r.signal,candleCount:ref.length};
 }
 if(!best)return wait("কোনো independently verified রেফারেন্স মেলেনি।");
 if(best.score<minScore)return wait("মিলের স্কোর নির্ধারিত সীমার নিচে।");
 return best;
}
const api={shape,match};
if(typeof module!=="undefined"&&module.exports)module.exports=api;
root.Q99ReferenceMatcher=api;
})(typeof globalThis!=="undefined"?globalThis:this);
