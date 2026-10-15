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
  $("minimize").addEventListener("click", event => {
    event.preventDefault();
    event.stopPropagation();
    minimize();
  });
  $("minimize-hint").addEventListener("click", minimize);
  let drag = null;
  let dragged = false;
  terminal.addEventListener("pointerdown", event => {
    if (!terminal.classList.contains("collapsed")) return;
    drag = { x: event.clientX, y: event.clientY, left: terminal.getBoundingClientRect().left, top: terminal.getBoundingClientRect().top };
    dragged = false;
    terminal.setPointerCapture?.(event.pointerId);
    event.preventDefault();
  });
  terminal.addEventListener("pointermove", event => {
    if (!drag || !terminal.classList.contains("collapsed")) return;
    const dx = event.clientX - drag.x, dy = event.clientY - drag.y;
    if (Math.abs(dx) + Math.abs(dy) > 5) dragged = true;
    if (dragged) {
      const x = Math.max(0, Math.min(window.innerWidth - terminal.offsetWidth, drag.left + dx));
      const y = Math.max(0, Math.min(window.innerHeight - terminal.offsetHeight, drag.top + dy));
      terminal.style.left = x + "px";
      terminal.style.top = y + "px";
      terminal.style.right = "auto";
      terminal.style.bottom = "auto";
    }
  });
  terminal.addEventListener("pointerup", () => { drag = null; });
  terminal.addEventListener("pointercancel", () => { drag = null; });
  terminal.addEventListener("click", event => {
    if (!terminal.classList.contains("collapsed")) return;
    if (dragged) { dragged = false; event.preventDefault(); event.stopPropagation(); return; }
    terminal.classList.remove("collapsed");
    terminal.style.left = "";
    terminal.style.top = "";
    terminal.style.right = "";
    terminal.style.bottom = "";
    event.preventDefault();
    event.stopPropagation();
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