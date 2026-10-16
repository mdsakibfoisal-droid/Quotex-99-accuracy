/* Q99 candle-pattern matcher.
 * Compares supplied OHLC candles against manually verified references.
 * It does not read Quotex's protected chart or discover live candles.
 */
(function(root){
"use strict";
function valid(c){return Array.isArray(c)&&c.length>=2&&c.every(x=>x&&["open","high","low","close"].every(k=>Number.isFinite(x[k]))&&x.high>=Math.max(x.open,x.close)&&x.low<=Math.min(x.open,x.close)&&x.high>=x.low)}
function shape(c){const r=c.high-c.low||1;return {dir:c.close>c.open?1:c.close<c.open?-1:0,body:Math.abs(c.close-c.open)/r,upper:(c.high-Math.max(c.open,c.close))/r,lower:(Math.min(c.open,c.close)-c.low)/r}}
function similarity(a,b){if(a.length!==b.length)return 0;let err=0;for(let i=0;i<a.length;i++){const x=shape(a[i]),y=shape(b[i]);err+=(x.dir===y.dir?0:.45)+Math.abs(x.body-y.body)*.25+Math.abs(x.upper-y.upper)*.15+Math.abs(x.lower-y.lower)*.15}return Math.max(0,Math.round((1-err/a.length)*100))}
function match(candles,refs,opts){opts=opts||{};const threshold=Number.isFinite(opts.threshold)?opts.threshold:90;if(!valid(candles))return {status:"WAIT",reason:"কমপক্ষে ২টি বৈধ OHLC ক্যান্ডেল দরকার।",match:0,signal:null};if(!Array.isArray(refs)||!refs.length)return {status:"WAIT",reason:"যাচাইকৃত রেফারেন্স প্যাটার্ন এখনো যোগ করা হয়নি।",match:0,signal:null};let best=null;for(const r of refs){if(!r||r.verified!==true||!valid(r.candles)||r.candles.length>candles.length||!["UP","DOWN","WAIT"].includes(r.outcome))continue;const score=similarity(candles.slice(-r.candles.length),r.candles);if(!best||score>best.match)best={id:r.id||"pattern",label:r.label||"রেফারেন্স প্যাটার্ন",match:score,outcome:r.outcome,source:r.source||"manual"}}
if(!best)return {status:"WAIT",reason:"ব্যবহারযোগ্য রেফারেন্স প্যাটার্ন নেই।",match:0,signal:null};
if(best.match<threshold||best.outcome==="WAIT")return {status:"WAIT",reason:"মিলের সীমা পূরণ হয়নি; কোনো সংকেত নয়।",match:best.match,signal:null,best};
return {status:"MATCH",reason:"গঠনগত মিল পাওয়া গেছে; ভবিষ্যৎ ফলের নিশ্চয়তা নয়।",match:best.match,signal:best.outcome,best,disclaimer:"মিলের স্কোর জেতার সম্ভাবনা নয়। লাইভ ডেটা আলাদাভাবে যাচাই করতে হবে।"}}
const api={valid,shape,similarity,match};if(typeof module!=="undefined"&&module.exports)module.exports=api;root.Q99PatternMatcher=api;
})(typeof globalThis!=="undefined"?globalThis:this);
