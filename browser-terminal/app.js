(() => {
  "use strict";
  const $ = id => document.getElementById(id);
  const target = $("target"), duration = $("duration");
  const status = $("status"), left = $("left"), count = $("intercepts");
  const terminal = $("terminal"), stopButton = $("stop");
  let timer = null, endAt = 0, intercepts = 0, activeTarget = "";
  const setStatus = text => { status.textContent = text; };
  function stop(message = "STOPPED") {
    if (timer !== null) clearInterval(timer);
    timer = null; endAt = 0; left.textContent = "—";
    stopButton.disabled = true;
    setStatus(message);
  }
  function minimize() { terminal.classList.add("collapsed"); }
  terminal.addEventListener("click", event => {
    if (terminal.classList.contains("collapsed")) {
      terminal.classList.remove("collapsed");
      event.stopPropagation();
    }
  });
  $("minimize").addEventListener("click", minimize);
  $("minimize-hint").addEventListener("click", minimize);
  $("key").addEventListener("click", () => {
    const notice = $("notice");
    notice.style.display = notice.style.display === "block" ? "none" : "block";
  });
  document.querySelectorAll("[data-seconds]").forEach(button => button.addEventListener("click", () => {
    duration.value = button.dataset.seconds;
    document.querySelectorAll("[data-seconds]").forEach(b => b.classList.toggle("active", b === button));
  }));
  $("start").addEventListener("click", () => {
    if (timer !== null) stop("STANDBY");
    const raw = target.value.trim();
    const seconds = Number(duration.value);
    const n = Number(raw);
    if (!raw || !Number.isFinite(n) || !Number.isFinite(seconds) || seconds < 1 || seconds > 300) {
      setStatus("CHECK INPUT"); return;
    }
    activeTarget = n.toFixed(Math.min(8, (raw.split(".")[1] || "").length));
    endAt = Date.now() + seconds * 1000;
    stopButton.disabled = false;
    setStatus("RUNNING");
    const tick = () => {
      const remaining = Math.max(0, endAt - Date.now());
      if (remaining <= 0) {
        stop("COMPLETE");
        intercepts += 1;
        count.textContent = String(intercepts);
      }
    };
    tick();
    if (endAt > Date.now()) timer = setInterval(tick, 100);
  });
  $("stop").addEventListener("click", () => stop());
  window.addEventListener("pagehide", () => { if (timer !== null) clearInterval(timer); });
})();