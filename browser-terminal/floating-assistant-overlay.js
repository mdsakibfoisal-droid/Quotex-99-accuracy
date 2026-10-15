/* Floating assistant overlay for the current browser page.
 * Install by adding <script src="floating-assistant-overlay.js"></script> to a test page,
 * or paste the contents into DevTools on a page you control.
 * A webpage script cannot float over other Chrome tabs. It does not read protected
 * broker data or place orders; it only checks for visible chart-like elements. */
(() => {
  "use strict";
  const id = "quotex-assistant-floating-overlay";
  document.getElementById(id)?.remove();
  const host = document.createElement("div");
  host.id = id;
  host.style.cssText = "position:fixed;left:18px;top:35vh;z-index:2147483647;font:14px system-ui,Arial;color:#fff";
  host.innerHTML = `
    <button id="qfa-bubble" aria-label="Open market assistant" style="width:58px;height:58px;border:2px solid #fff;border-radius:50%;background:#087fce;color:#fff;font-weight:bold;font-size:20px;box-shadow:0 3px 15px #0008;touch-action:none">Q99</button>
    <section id="qfa-panel" style="display:none;position:absolute;left:0;top:66px;width:min(320px,calc(100vw - 24px));max-height:65vh;overflow:auto;background:#102538;border:1px solid #2e9ed0;border-radius:14px;padding:14px;box-shadow:0 4px 20px #0009;box-sizing:border-box">
      <div style="display:flex;align-items:center;justify-content:space-between;gap:8px"><b>বাংলা চার্ট সহকারী</b><button id="qfa-close" style="padding:6px">✕</button></div>
      <p style="font-size:12px;line-height:1.5">আইকনটি চেপে ধরে স্ক্রিনের যেকোনো জায়গায় টেনে নিতে পারো। এটি শুধু এই পেজে থাকবে।</p>
      <button id="qfa-scan" style="padding:9px;border:0;border-radius:8px;background:#168c72;color:#fff">চার্ট পরীক্ষা</button>
      <div id="qfa-result" style="white-space:pre-wrap;line-height:1.55;margin-top:10px;font-size:13px">এখনো পরীক্ষা করা হয়নি।</div>
      <hr style="border-color:#315066">
      <label for="qfa-question">প্রশ্ন লিখুন</label>
      <input id="qfa-question" style="width:100%;box-sizing:border-box;padding:9px;margin:6px 0;background:#071321;color:white;border:1px solid #456;border-radius:7px" placeholder="যেমন: শেষ ক্যান্ডেল কী রঙের?">
      <button id="qfa-ask" style="padding:9px;border:0;border-radius:8px">উত্তর</button>
      <p id="qfa-answer" style="font-size:13px;line-height:1.55"></p>
      <small style="color:#b7c8d8;line-height:1.5">সীমা: চার্ট-এলিমেন্ট পাওয়া মানে ক্যান্ডেল পড়তে পারা নয়। এখানে লাইভ OHLC, ক্রেতা-বিক্রেতার অর্ডার-ফ্লো বা ট্রেডিং অ্যাকশন নেই।</small>
    </section>`;
  document.documentElement.appendChild(host);
  const $ = s => host.querySelector(s);
  let drag = null, dragged = false;
  const bubble = $("#qfa-bubble"), panel = $("#qfa-panel");
  bubble.addEventListener("pointerdown", e => {
    const r = host.getBoundingClientRect();
    drag = { x:e.clientX, y:e.clientY, left:r.left, top:r.top };
    dragged = false;
    bubble.setPointerCapture(e.pointerId);
  });
  bubble.addEventListener("pointermove", e => {
    if (!drag) return;
    const dx=e.clientX-drag.x, dy=e.clientY-drag.y;
    if (Math.abs(dx)>4 || Math.abs(dy)>4) dragged=true;
    if (dragged) {
      const x=Math.max(0,Math.min(innerWidth-58,drag.left+dx));
      const y=Math.max(0,Math.min(innerHeight-58,drag.top+dy));
      host.style.left=x+"px"; host.style.top=y+"px";
      host.style.right="auto"; host.style.bottom="auto";
    }
  });
  bubble.addEventListener("pointerup",()=>{drag=null;});
  bubble.addEventListener("click",()=>{if(dragged){dragged=false;return;}panel.style.display=panel.style.display==="none"?"block":"none";});
  $("#qfa-close").onclick=()=>panel.style.display="none";
  function scan() {
    const matches=[...document.querySelectorAll("canvas,svg,[class*='chart' i],[id*='chart' i],[class*='tradingview' i]")]
      .filter(el=>!host.contains(el)&&el.getBoundingClientRect().width>80&&el.getBoundingClientRect().height>50&&getComputedStyle(el).visibility!=="hidden");
    const text=matches.length
      ? "চার্টের মতো "+matches.length+"টি দৃশ্যমান এলিমেন্ট পাওয়া গেছে। এটি ক্যান্ডেলের সংখ্যা নয়। এই পেজ থেকে আসল OHLC ডেটা নিশ্চিত করা যায়নি; তাই ক্যান্ডেলের রং, উইক বা CALL/PUT অনুমান করছি না।"
      : "দৃশ্যমান চার্ট-এলিমেন্ট শনাক্ত হয়নি। চার্ট লোড হয়েছে কি না পরীক্ষা করুন।";
    $("#qfa-result").textContent=text;
    return text;
  }
  $("#qfa-scan").onclick=scan;
  $("#qfa-ask").onclick=()=>{
    const q=$("#qfa-question").value.trim().toLowerCase();
    let a;
    if(!q) a="প্রশ্ন লিখুন।";
    else if(/চার্ট|দেখতে|দেখছ/.test(q)) a=scan();
    else if(/ক্যান্ডেল|লাল|সবুজ|বডি|উইক|wick|body|কতটা|কয়টা/.test(q)) a="এই পেজ থেকে আসল ক্যান্ডেল/OHLC ডেটা পড়া যাচ্ছে না। চার্টের ডেটা বা পরিষ্কার স্ক্রিনশট ছাড়া ক্যান্ডেল সংখ্যা, রং, বডি বা উইক নিশ্চিতভাবে বলা ঠিক হবে না।";
    else if(/কল|পুট|call|put|ট্রেড|সিগন্যাল/.test(q)) a="লাইভ ও যাচাইকৃত ডেটা নেই, তাই CALL/PUT সিগন্যাল বা স্বয়ংক্রিয় ট্রেড করা হচ্ছে না।";
    else a="আমি এই পেজে চার্ট-এলিমেন্ট শনাক্ত করতে পারি। লাইভ AI বা ব্রোকার ডেটা সংযোগ নেই।";
    $("#qfa-answer").textContent=a;
    if("speechSynthesis" in window){speechSynthesis.cancel();const u=new SpeechSynthesisUtterance(a);u.lang="bn-BD";speechSynthesis.speak(u);}
  };
})();