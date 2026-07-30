(function () {
    /* global L */

    var dataEl = document.getElementById('map-data');
    if (!dataEl) return;

    var data = JSON.parse(dataEl.textContent);
    var locations = data.locations || [];
    var layerLabels = data.layerLabels || {};
    var labels = data.labels || {};
    var forecastUrl = data.forecastUrl || '/forecast';

    var DEFAULT_CENTER = [20, 0];
    var DEFAULT_ZOOM = 2;

    // CARTO's free basemaps (built on OSM data) look considerably nicer
    // than the stock OSM tiles and, unlike them, come in a matching
    // light/dark pair so the map can follow the app's own theme toggle.
    var BASEMAPS = {
        light: {
            url: 'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
        },
        dark: {
            url: 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png',
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
        }
    };

    function currentTheme() {
        return document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
    }

    function escapeHtml(value) {
        var entities = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
        return String(value).replace(/[&<>"']/g, function (ch) {
            return entities[ch];
        });
    }

    function markerIcon() {
        return L.divIcon({
            className: 'weather-map-marker',
            html: '<span class="weather-map-marker-pin"><i class="bi bi-geo-alt-fill"></i></span>',
            iconSize: [30, 30],
            iconAnchor: [15, 30],
            popupAnchor: [0, -28]
        });
    }

    function popupHtml(location) {
        var href = forecastUrl + '?lat=' + encodeURIComponent(location.latitude) + '&lon=' + encodeURIComponent(location.longitude);
        return '<div class="weather-map-popup">' +
            '<div class="weather-map-popup-title">' + escapeHtml(location.name) + '</div>' +
            '<a class="weather-map-popup-link" href="' + href + '">' +
            '<i class="bi bi-arrow-right-circle"></i> ' + escapeHtml(labels.forecast || 'View forecast') +
            '</a></div>';
    }

    var MIN_ZOOM = 2;

    var map = L.map('weather-map', { zoomControl: false, minZoom: MIN_ZOOM }).setView(DEFAULT_CENTER, DEFAULT_ZOOM);
    L.control.zoom({ position: 'topleft' }).addTo(map);
    L.control.scale({ imperial: true, metric: true, position: 'bottomleft' }).addTo(map);

    var baseLayer = L.tileLayer(BASEMAPS[currentTheme()].url, {
        maxZoom: 20,
        subdomains: 'abcd',
        attribution: BASEMAPS[currentTheme()].attribution
    }).addTo(map);

    var themeObserver = new MutationObserver(function () {
        var theme = currentTheme();
        var next = BASEMAPS[theme];
        map.removeLayer(baseLayer);
        baseLayer = L.tileLayer(next.url, {
            maxZoom: 20,
            subdomains: 'abcd',
            attribution: next.attribution
        }).addTo(map);
        baseLayer.bringToBack();
    });
    themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-bs-theme'] });

    var overlayOptions = { opacity: 0.55, maxZoom: 18 };
    var overlays = {};
    overlays[layerLabels.precipitation] = L.tileLayer('/map/tiles/precipitation/{z}/{x}/{y}', overlayOptions);
    overlays[layerLabels.clouds] = L.tileLayer('/map/tiles/clouds/{z}/{x}/{y}', overlayOptions);
    overlays[layerLabels.temperature] = L.tileLayer('/map/tiles/temperature/{z}/{x}/{y}', overlayOptions);
    overlays[layerLabels.wind] = L.tileLayer('/map/tiles/wind/{z}/{x}/{y}', overlayOptions);

    L.control.layers(null, overlays, { collapsed: false, position: 'topright' }).addTo(map);

    var markers = [];
    locations.forEach(function (location) {
        if (location.latitude == null || location.longitude == null) {
            return;
        }
        var marker = L.marker([location.latitude, location.longitude], { icon: markerIcon() })
            .addTo(map)
            .bindPopup(popupHtml(location));
        markers.push(marker);
    });

    if (markers.length > 0) {
        var group = L.featureGroup(markers);
        map.fitBounds(group.getBounds().pad(0.3));
    }
})();