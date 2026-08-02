document.addEventListener('DOMContentLoaded', () => {
    const root = document.getElementById('dashboardLiveRoot');
    if (!root || typeof StompJs === 'undefined') {
        return;
    }

    const sort = root.dataset.sort;
    const page = parseInt(root.dataset.page, 10) || 0;
    const tempSymbol = root.dataset.tempSymbol;
    const windUnit = root.dataset.windUnit;
    const i18n = {
        feelsLike: root.dataset.i18nFeelsLike,
        min: root.dataset.i18nMin,
        max: root.dataset.i18nMax,
        clouds: root.dataset.i18nClouds,
        wind: root.dataset.i18nWind,
        humidity: root.dataset.i18nHumidity,
        pressure: root.dataset.i18nPressure,
    };

    /** Minimal {0}/{1}/{2}-style substitution, matching the java.text.MessageFormat patterns from messages.properties. */
    function formatMessage(pattern, ...args) {
        return pattern.replace(/\{(\d+)}/g, (_, index) => args[index]);
    }

    function formatTime(epochMillis) {
        const date = new Date(epochMillis);
        const pad = n => String(n).padStart(2, '0');
        return `${pad(date.getUTCHours())}:${pad(date.getUTCMinutes())}:${pad(date.getUTCSeconds())}`;
    }

    function patchCard(card, weather) {
        const set = (field, text) => {
            const el = card.querySelector(`[data-field="${field}"]`);
            if (el) {
                el.textContent = text;
            }
        };

        set('time', formatTime(weather.date));
        set('temperature', `${weather.temperature}${tempSymbol}`);
        set('feelsLike', formatMessage(i18n.feelsLike, weather.temperatureFeelsLike, tempSymbol));
        set('description', weather.description.charAt(0).toUpperCase() + weather.description.slice(1));
        set('min', formatMessage(i18n.min, weather.temperatureMinimum, tempSymbol));
        set('clouds', formatMessage(i18n.clouds, weather.cloudiness));
        set('wind', formatMessage(i18n.wind, weather.windDirection, weather.windSpeed, windUnit));
        set('max', formatMessage(i18n.max, weather.temperatureMaximum, tempSymbol));
        set('humidity', formatMessage(i18n.humidity, weather.humidity));
        set('pressure', formatMessage(i18n.pressure, weather.pressure));

        const icon = card.querySelector('[data-field="icon"]');
        if (icon) {
            icon.src = `/images/${weather.weatherCondition}-${weather.timeOfDay}.svg`;
        }

        card.classList.add('live-updated');
        setTimeout(() => card.classList.remove('live-updated'), 1500);
    }

    LiveWeather.connect(client => {
        client.subscribe('/user/queue/dashboard', message => {
            const update = JSON.parse(message.body);
            (update.locations || []).forEach(location => {
                const card = root.querySelector(`[data-location-id="${location.locationId}"]`);
                if (card && location.weather) {
                    patchCard(card, location.weather);
                }
            });
        });

        client.publish({
            destination: '/app/dashboard.subscribe',
            body: JSON.stringify({ sort, page }),
        });
    });
});