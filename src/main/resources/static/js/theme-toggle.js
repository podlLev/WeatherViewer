(function () {
    function syncButtonState() {
        var button = document.getElementById('theme-toggle-btn');
        if (!button) return;

        var currentTheme = document.documentElement.getAttribute('data-bs-theme') || 'light';
        var icon = button.querySelector('i');

        if (currentTheme === 'dark') {
            if (icon) icon.className = 'bi bi-sun-fill';
            button.setAttribute('aria-label', 'Switch to light mode');
            button.setAttribute('title', 'Switch to light mode');
        } else {
            if (icon) icon.className = 'bi bi-moon-stars-fill';
            button.setAttribute('aria-label', 'Switch to dark mode');
            button.setAttribute('title', 'Switch to dark mode');
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', syncButtonState);
    } else {
        syncButtonState();
    }

    document.addEventListener('click', function (event) {
        var button = event.target.closest('#theme-toggle-btn');
        if (!button) return;

        var currentTheme = document.documentElement.getAttribute('data-bs-theme') || 'light';
        var nextTheme = currentTheme === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-bs-theme', nextTheme);

        try {
            window.localStorage.setItem('theme', nextTheme);
        } catch (e) {
        }
        syncButtonState();
    });
})();