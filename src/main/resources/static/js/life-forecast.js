document.addEventListener('DOMContentLoaded', () => {
    const root = document.getElementById('forecastLiveRoot');
    if (!root || typeof StompJs === 'undefined') {
        return;
    }

    const lat = parseFloat(root.dataset.lat);
    const lon = parseFloat(root.dataset.lon);
    const tempSymbol = root.dataset.tempSymbol;
    const locale = root.dataset.locale || undefined;

    /** enum name (e.g. "RAIN") -> localized weather-condition.* label, from ForecastController. */
    const conditionLabels = JSON.parse(document.getElementById('forecast-condition-labels').textContent || '{}');

    const dayDateFormatter = new Intl.DateTimeFormat(locale, { day: '2-digit', month: 'long' });
    const dayNameFormatter = new Intl.DateTimeFormat(locale, { weekday: 'long' });
    const hourFormatter = new Intl.DateTimeFormat(locale, { hour: '2-digit', minute: '2-digit', hour12: false });

    function set(card, field, text) {
        const el = card.querySelector(`[data-field="${field}"]`);
        if (el) {
            el.textContent = text;
        }
    }

    function setIcon(card, weather) {
        const icon = card.querySelector('[data-field="icon"]');
        if (icon) {
            icon.src = `/images/${weather.weatherCondition}-${weather.timeOfDay}.svg`;
            icon.alt = `${weather.weatherCondition} ${weather.timeOfDay}`;
        }
    }

    function setDescription(card, weather) {
        const el = card.querySelector('[data-field="description"]');
        if (el) {
            el.textContent = conditionLabels[weather.weatherCondition] || weather.weatherCondition;
            el.dataset.condition = weather.weatherCondition;
        }
    }

    function flash(card) {
        card.classList.add('live-updated');
        setTimeout(() => card.classList.remove('live-updated'), 1500);
    }

    function patchHourlyCard(card, weather) {
        set(card, 'time', hourFormatter.format(new Date(weather.date)));
        set(card, 'temperature', `${weather.temperature}${tempSymbol}`);
        setIcon(card, weather);
        setDescription(card, weather);
        flash(card);
    }

    function patchDailyCard(card, weather) {
        const date = new Date(weather.date);
        set(card, 'dayDate', dayDateFormatter.format(date));
        set(card, 'dayName', dayNameFormatter.format(date));
        set(card, 'temperature', `${weather.temperature}${tempSymbol}`);
        set(card, 'temperatureMinimum', `${weather.temperatureMinimum}${tempSymbol}`);
        setIcon(card, weather);
        setDescription(card, weather);
        flash(card);
    }

    function patchForecast(update) {
        (update.hourlyForecast || []).slice(0, 5).forEach((weather, index) => {
            const card = root.querySelector(`[data-hourly-index="${index}"]`);
            if (card) {
                patchHourlyCard(card, weather);
            }
        });

        (update.dailyForecast || []).forEach((weather, index) => {
            const card = root.querySelector(`[data-daily-index="${index}"]`);
            if (card) {
                patchDailyCard(card, weather);
            }
        });
    }

    LiveWeather.connect(client => {
        client.subscribe('/user/queue/forecast', message => {
            patchForecast(JSON.parse(message.body));
        });

        client.publish({
            destination: '/app/forecast.subscribe',
            body: JSON.stringify({ lat, lon }),
        });
    });
});