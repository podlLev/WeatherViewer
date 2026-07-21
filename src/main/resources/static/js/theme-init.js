/*
 * Runs synchronously in <head>, before the body is parsed, so the page
 * never flashes the wrong color mode. Precedence: an explicit choice saved
 * by theme-toggle.js, then the OS-level preference, then light.
 */
(function () {
    try {
        var saved = window.localStorage.getItem('theme');
        var theme = saved === 'dark' || saved === 'light'
            ? saved
            : (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');

        document.documentElement.setAttribute('data-bs-theme', theme);
    } catch (e) {
        document.documentElement.setAttribute('data-bs-theme', 'light');
    }
})();