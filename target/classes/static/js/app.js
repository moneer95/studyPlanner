(function () {
  var TOAST_MS = 5000;

  /** Pools keyed by `data-suggest`; each click advances that field to its next value. */
  window.FIELD_SUGGESTIONS = {
    loginUser: ["student", "tutor", "admin"],
    loginPass: ["student123", "tutor123", "admin123"],
    regUser: ["teststudent", "testlearner", "demo_user"],
    regEmail: ["teststudent@example.com", "learner@example.com", "demo@mail.test"],
    regPass: ["testpass123", "password123", "changeme99"],
    planTitle: [
      "Week of April 14 (test)",
      "Spring finals sprint",
      "Summer catch-up plan",
      "Night-before review block",
    ],
    examTitle: ["Midterm Biology", "Oral defense", "License exam", "Project hand-in"],
    examDeadline: [
      "2026-04-30T17:00",
      "2026-05-15T09:00",
      "2026-06-01T14:30",
      "2026-06-20T23:59",
    ],
    sessionTopic: [
      "Arithmetic review",
      "Reading comprehension",
      "Mock exam walkthrough",
      "Essay outline workshop",
    ],
    sessionStart: [
      "2026-04-14T10:00",
      "2026-04-16T15:00",
      "2026-04-18T09:30",
      "2026-04-22T13:00",
    ],
    sessionEnd: [
      "2026-04-14T11:00",
      "2026-04-16T16:30",
      "2026-04-18T11:00",
      "2026-04-22T14:30",
    ],
    qText: [
      "What is 2 + 2?",
      "Capital of France?",
      "Solve: 12 ÷ 3",
      "Which planet is known as the Red Planet?",
    ],
    optA: ["4", "Paris", "4", "Mars"],
    optB: ["3", "Lyon", "3", "Venus"],
    optC: ["22", "Marseille", "6", "Jupiter"],
    optD: ["5", "Nice", "5", "Saturn"],
    qTopic: ["Arithmetic", "Geography", "Arithmetic", "Reading"],
    correctIndex: ["0", "1", "2", "3"],
    tutorPlanTitle: [
      "Spring review plan",
      "Exam prep intensive",
      "Catch-up after absence",
      "Parent meeting follow-up",
    ],
    interventionNotes: [
      "Reviewed weak topics; assigned practice quiz and follow-up next week.",
      "Discussed time management; shared calendar template.",
      "Recommended office hours; student to attend Tuesday.",
      "Positive progress; encouraged to keep current pace.",
    ],
  };

  var suggestionCursorByEl = new WeakMap();

  function advanceFieldSuggestions() {
    var pools = window.FIELD_SUGGESTIONS || {};
    document.querySelectorAll("[data-suggest]").forEach(function (el) {
      var key = el.getAttribute("data-suggest");
      var arr = pools[key];
      if (!arr || !arr.length) return;
      var idx = suggestionCursorByEl.get(el);
      if (idx == null) idx = -1;
      idx = (idx + 1) % arr.length;
      suggestionCursorByEl.set(el, idx);
      el.value = arr[idx];
    });
    document.querySelectorAll("[data-suggest-select]").forEach(function (sel) {
      if (!(sel instanceof HTMLSelectElement) || !sel.options.length) return;
      var idx = suggestionCursorByEl.get(sel);
      if (idx == null) idx = -1;
      idx = (idx + 1) % sel.options.length;
      suggestionCursorByEl.set(sel, idx);
      sel.selectedIndex = idx;
    });
  }

  function ensureSuggestToolbar() {
    var bar = document.getElementById("app-suggest-toolbar");
    if (!bar) {
      bar = document.createElement("div");
      bar.id = "app-suggest-toolbar";
      bar.setAttribute("aria-label", "Test field suggestions");
      var btn = document.createElement("button");
      btn.type = "button";
      btn.id = "app-suggest-next-btn";
      btn.className = "app-suggest-btn";
      btn.textContent = "Next suggestions";
      btn.title = "Cycle each field to the next value in its suggestion list";
      btn.addEventListener("click", function () {
        advanceFieldSuggestions();
      });
      bar.appendChild(btn);
      document.body.appendChild(bar);
    }
    return bar;
  }

  function refreshSuggestionToolbar() {
    var n =
      document.querySelectorAll("[data-suggest], [data-suggest-select]").length;
    var bar = document.getElementById("app-suggest-toolbar");
    if (n === 0) {
      if (bar) bar.hidden = true;
      return;
    }
    bar = ensureSuggestToolbar();
    bar.hidden = false;
  }

  document.addEventListener("DOMContentLoaded", refreshSuggestionToolbar);

  function toastRoot() {
    var el = document.getElementById("app-toast-root");
    if (!el) {
      el = document.createElement("div");
      el.id = "app-toast-root";
      el.setAttribute("aria-live", "polite");
      document.body.appendChild(el);
    }
    return el;
  }

  window.showAppToast = function (message, variant) {
    if (!message) return;
    var root = toastRoot();
    var t = document.createElement("div");
    t.className = "app-toast" + (variant === "error" ? " app-toast-error" : "");
    t.textContent = message;
    root.appendChild(t);
    requestAnimationFrame(function () {
      t.classList.add("app-toast-visible");
    });
    setTimeout(function () {
      t.classList.remove("app-toast-visible");
      setTimeout(function () {
        t.remove();
      }, 280);
    }, TOAST_MS);
  };

  function swapFromDocument(doc, path) {
    var newHeader = doc.querySelector("header");
    var newMain = doc.querySelector("main");
    var curHeader = document.querySelector("header");
    var curMain = document.querySelector("main");
    if (newHeader && curHeader) {
      curHeader.replaceWith(newHeader);
    }
    if (newMain && curMain) {
      curMain.replaceWith(newMain);
    } else if (newMain && !curMain) {
      document.body.appendChild(newMain);
    } else if (!newMain || !curMain) {
      window.location.href = path;
      return;
    }
    if (doc.title) {
      document.title = doc.title;
    }
    window.history.pushState({}, "", path);
    refreshSuggestionToolbar();
  }

  document.addEventListener(
    "submit",
    function (e) {
      var form = e.target;
      if (!(form instanceof HTMLFormElement)) return;
      if (form.method.toLowerCase() !== "post") return;
      if (form.getAttribute("data-ajax") !== "true") return;

      var confirmMsg = form.getAttribute("data-confirm");
      if (confirmMsg && !window.confirm(confirmMsg)) {
        e.preventDefault();
        return;
      }

      e.preventDefault();

      var action = form.action;
      var fd = new FormData(form);

      fetch(action, {
        method: "POST",
        body: fd,
        credentials: "same-origin",
        headers: {
          "X-Ajax-Form": "1",
          Accept: "application/json",
        },
      })
        .then(function (res) {
          var ct = res.headers.get("content-type") || "";
          if (res.ok && ct.indexOf("application/json") !== -1) {
            return res.json().then(function (data) {
              showAppToast(data.toast || "Done");
              if (data.path) {
                return fetch(data.path, {
                  credentials: "same-origin",
                  headers: { Accept: "text/html" },
                }).then(function (pageRes) {
                  if (!pageRes.ok) {
                    window.location.href = data.path;
                    return;
                  }
                  return pageRes.text().then(function (html) {
                    var parser = new DOMParser();
                    var doc = parser.parseFromString(html, "text/html");
                    swapFromDocument(doc, data.path);
                  });
                });
              }
            });
          }
          if (res.redirected) {
            window.location.href = res.url;
            return;
          }
          if (res.status === 401 || res.status === 403) {
            window.location.href = "/login";
            return;
          }
          window.location.reload();
        })
        .catch(function () {
          showAppToast("Something went wrong. Please try again.", "error");
        });
    },
    false
  );
})();
