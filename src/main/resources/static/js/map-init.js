/* global L */
(function () {
    'use strict';

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    var dataElement = document.getElementById('map-data');
    var data = dataElement
        ? JSON.parse(dataElement.textContent)
        : { locations: [], layerLabels: {}, labels: {}, forecastUrl: '/forecast', addLocationUrl: '/search/add' };

    var locations = data.locations || [];
    var layerLabels = data.layerLabels || {};
    var labels = data.labels || {};
    var forecastUrl = data.forecastUrl || '/forecast';
    var addLocationUrl = data.addLocationUrl || '/search/add';
    var csrfParam = data.csrfParam || '_csrf';
    var csrfToken = data.csrfToken || getCsrfToken();

    function getCsrfToken() {
        var match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
        return match ? decodeURIComponent(match[1]) : '';
    }

    var DEFAULT_CENTER = [20, 0];
    var DEFAULT_ZOOM = 2;

    var map = L.map('weather-map').setView(DEFAULT_CENTER, DEFAULT_ZOOM);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; OpenStreetMap contributors',
        className: 'base-map-tiles'
    }).addTo(map);

    var overlayOptions = { opacity: 0.55, maxZoom: 18 };
    var overlays = {};
    overlays[layerLabels.precipitation] = L.tileLayer('/map/tiles/precipitation/{z}/{x}/{y}', overlayOptions);
    overlays[layerLabels.clouds] = L.tileLayer('/map/tiles/clouds/{z}/{x}/{y}', overlayOptions);
    overlays[layerLabels.temperature] = L.tileLayer('/map/tiles/temperature/{z}/{x}/{y}', overlayOptions);
    overlays[layerLabels.wind] = L.tileLayer('/map/tiles/wind/{z}/{x}/{y}', overlayOptions);

    L.control.layers(null, overlays, { collapsed: false }).addTo(map);

    var markers = [];
    locations.forEach(function (location) {
        if (location.latitude == null || location.longitude == null) {
            return;
        }

        var forecastLink = '<a href="' + forecastUrl +
            '?lat=' + encodeURIComponent(location.latitude) +
            '&lon=' + encodeURIComponent(location.longitude) + '">' +
            escapeHtml(labels.forecast || 'Forecast') + '</a>';
        var popupContent = '<strong>' + escapeHtml(location.name) + '</strong><br>' + forecastLink;

        var marker = L.marker([location.latitude, location.longitude], { bubblingMouseEvents: false })
            .addTo(map)
            .bindPopup(popupContent);
        markers.push(marker);
    });

    if (markers.length > 0) {
        var group = L.featureGroup(markers);
        map.fitBounds(group.getBounds().pad(0.3));
    }

    map.on('click', function (e) {
        var lat = e.latlng.lat;
        var lng = e.latlng.lng;

        var formHtml =
            '<form action="' + addLocationUrl + '" method="post" class="map-add-location-form">' +
            '<div class="mb-2">' +
            '<input type="text" name="name" class="form-control form-control-sm" ' +
            'placeholder="' + escapeHtml(labels.locationNamePlaceholder || 'Location name') + '" ' +
            'required maxlength="100">' +
            '</div>' +
            '<input type="hidden" name="latitude" value="' + lat + '">' +
            '<input type="hidden" name="longitude" value="' + lng + '">' +
            '<input type="hidden" name="' + escapeHtml(csrfParam) + '" value="' + escapeHtml(csrfToken) + '">' +
            '<button type="submit" class="btn btn-sm btn-primary">' +
            escapeHtml(labels.addLocation || 'Add location') +
            '</button>' +
            '</form>';

        L.popup()
            .setLatLng(e.latlng)
            .setContent(formHtml)
            .openOn(map);
    });
})();