/* Q99 candle-pattern matcher.
 * Supports verified numeric OHLC or normalized hand-transcribed candle shapes.
 * Never reads Quotex's protected chart or discovers live candles by itself.
 */
(function(root){
"use strict";
function isNum(x){return Number.isFinite(x)}
function validOHLC(c){return Array.isArray(c)&&c.length>=2&&c.every(x=>x&&["open","high","low","close"].every(k=>isNum(x[k]))&&x.high>=Math.max(x.open,x.close)&&x.low<=Math.min(x.open,x.close)&&x.high>=x.low)}
function validShape(c){return Array.isArray(c)&&c.length>=2&&c.every(x=>x&&[-1,0,1].includes(x.dir)&&["body","upper","lower"].every(k=>isNum(x[k])&&x[k]>=0&&x[k]<=1.001)&&x.body+x.upper+x.lower<=1.01)}
function valid(c){return validOHLC(c)||validShape(c)}
function shape(c){if("dir"in c)return {dir:c.dir,body:c.body,upper:c.upper,lower:c.lower};const r=c.high-c.low||1;return {dir:c.close>c.open?1:c.close<c.open?-1:0,body:Math.abs(c.close-c.open)/r,upper:(c.high-Math.max(c.open,c.close))/r,lower:(Math.min(c.open,c.close)-c.low)/r}}
function similarity(a,b){if(a.length!==b.length||!valid(a)||!valid(b))return 0;let err=0;for(let i=0;i<a.length;i++){const x=shape(a[i]),y=shape(b[i]);err+=(x.dir===y.dir?0:.45)+Math.abs(x.body-y.body)*.25+Math.abs(x.upper-y.upper)*.15+Math.abs(x.lower-y.lower)*.15}return Math.max(0,Math.round((1-err/a.length)*100))}
function match(candles,refs,opts){opts=opts||{};const threshold=Number.isFinite(opts.threshold)?opts.threshold:90;if(!valid(candles))return {status:"WAIT",reason:"কমপক্ষে ২টি বৈধ OHLC অথবা যাচাইকৃত normalized candle feature দরকার।",match:0,signal:null};if(!Array.isArray(refs)||!refs.length)return {status:"WAIT",reason:"যাচাইকৃত রেফারেন্স প্যাটার্ন নেই।",match:0,signal:null};let best=null;for(const r of refs){if(!r||r.verified!==true||!valid(r.candles)||r.candles.length>candles.length||!["UP","DOWN","WAIT"].includes(r.outcome))continue;const score=similarity(candles.slice(-r.candles.length),r.candles);if(!best||score>best.match)best={id:r.id||"pattern",label:r.label||"রেফারেন্স প্যাটার্ন",match:score,outcome:r.outcome,source:r.source||"manual"}}
if(!best)return {status:"WAIT",reason:"ব্যবহারযোগ্য যাচাইকৃত রেফারেন্স প্যাটার্ন নেই।",match:0,signal:null};
if(best.match<threshold||best.outcome==="WAIT")return {status:"WAIT",reason:"মিলের সীমা পূরণ হয়নি; কোনো সংকেত নয়।",match:best.match,signal:null,best};
return {status:"MATCH",reason:"গঠনগত মিল পাওয়া গেছে; ভবিষ্যৎ ফলের নিশ্চয়তা নয়।",match:best.match,signal:best.outcome,best,disclaimer:"মিলের স্কোর জেতার সম্ভাবনা নয়। লাইভ ডেটা আলাদাভাবে যাচাই করতে হবে।"}}
const api={valid,validOHLC,validShape,shape,similarity,match};if(typeof module!=="undefined"&&module.exports)module.exports=api;root.Q99PatternMatcher=api;
})(typeof globalThis!=="undefined"?globalThis:this);
