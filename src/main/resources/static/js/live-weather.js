/**
 * Thin wrapper around StompJs.Client for this app's live-weather feeds.
 *
 * One WebSocket connection per page (native ws/wss, no SockJS - see
 * WebSocketConfig for why). Reconnects automatically with backoff and
 * re-runs onConnect (which callers use to re-send their subscribe frame)
 * every time, since the server-side subscription registry is in-memory and
 * doesn't survive a dropped connection.
 */
const LiveWeather = (() => {

    function connect(onConnectCallback) {
        const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
        const brokerUrl = `${protocol}://${window.location.host}/ws`;

        const client = new StompJs.Client({
            brokerURL: brokerUrl,
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
        });

        client.onConnect = () => onConnectCallback(client);

        client.onStompError = frame => {
            console.warn('Live weather STOMP error:', frame.headers && frame.headers.message);
        };

        client.activate();
        window.addEventListener('beforeunload', () => client.deactivate());
        return client;
    }

    return { connect };
})();