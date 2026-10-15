(() => {
  "use strict";
  const $ = id => document.getElementById(id);
  const target = $("target"), duration = $("duration");
  const status = $("status"), value = $("value"), left = $("left"), count = $("intercepts");
  let timer = null, endAt = 0, intercepts = 0;
  const setStatus = text => { status.textContent = text; };
  function stop(message = "STOPPED") {
    if (timer !== null) clearInterval(timer);
    timer = null; endAt = 0; left.textContent = "—"; setStatus(message);
  }
  document.querySelectorAll("[data-seconds]").forEach(button => button.addEventListener("click", () => {
    duration.value = button.dataset.seconds;
    document.querySelectorAll("[data-seconds]").forEach(b => b.classList.toggle("active", b === button));
  }));
  $("minimize").addEventListener("click", () => $("terminal").classList.toggle("collapsed"));
  $("start").addEventListener("click", () => {
    stop("STANDBY");
    const raw = target.value.trim(), seconds = Number(duration.value), n = Number(raw);
    if (!raw || !Number.isFinite(n) || !Number.isFinite(seconds) || seconds < 1 || seconds > 300) {
      setStatus("CHECK INPUT"); return;
    }
    value.textContent = n.toFixed(Math.min(8, (raw.split(".")[1] || "").length));
    endAt = Date.now() + seconds * 1000;
    setStatus("DEMO RUNNING");
    const tick = () => {
      const remaining = Math.max(0, endAt - Date.now());
      left.textContent = (remaining / 1000).toFixed(1) + "s";
      if (remaining <= 0) {
        stop("COMPLETE");
        intercepts += 1; count.textContent = String(intercepts);
      }
    };
    tick(); timer = setInterval(tick, 100);
  });
  $("stop").addEventListener("click", () => stop());
  window.addEventListener("pagehide", () => { if (timer !== null) clearInterval(timer); });
})();