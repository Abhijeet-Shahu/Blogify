(function () {
    "use strict";

    const storageKey = "blogify-theme";
    const root = document.documentElement;

    function applyTheme(theme) {
        root.dataset.theme = theme;
        document.querySelectorAll("[data-theme-toggle]").forEach((toggle) => {
            const isDark = theme === "dark";
            toggle.setAttribute("aria-pressed", String(isDark));
            toggle.setAttribute("aria-label", isDark ? "Switch to light theme" : "Switch to dark theme");
            const label = toggle.querySelector("[data-theme-label]");
            if (label) {
                label.textContent = isDark ? "Light" : "Dark";
            }
        });
    }

    function preferredTheme() {
        const savedTheme = window.localStorage.getItem(storageKey);
        if (savedTheme === "light" || savedTheme === "dark") {
            return savedTheme;
        }
        return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    }

    window.blogifyTheme = {
        set(theme) {
            if (theme !== "light" && theme !== "dark") {
                return;
            }
            window.localStorage.setItem(storageKey, theme);
            applyTheme(theme);
        },
        toggle() {
            this.set(root.dataset.theme === "dark" ? "light" : "dark");
        }
    };

    applyTheme(preferredTheme());

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll("[data-theme-toggle]").forEach((toggle) => {
            toggle.addEventListener("click", function () {
                window.blogifyTheme.toggle();
            });
        });

        document.querySelectorAll("[data-mobile-menu-toggle]").forEach((toggle) => {
            const menu = document.getElementById(toggle.getAttribute("aria-controls"));
            if (!menu) {
                return;
            }
            toggle.addEventListener("click", function () {
                const isOpen = !menu.classList.toggle("hidden");
                toggle.setAttribute("aria-expanded", String(isOpen));
            });
        });
    });
})();
