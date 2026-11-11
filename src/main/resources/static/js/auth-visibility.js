(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll("[data-password-toggle]").forEach(function (toggle) {
            const input = document.getElementById(toggle.getAttribute("aria-controls"));
            if (!input) {
                return;
            }
            toggle.addEventListener("click", function () {
                const visible = input.type === "password";
                input.type = visible ? "text" : "password";
                toggle.setAttribute("aria-pressed", String(visible));
                toggle.textContent = visible ? "Hide" : "Show";
            });
        });
    });
})();
