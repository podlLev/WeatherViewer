package com.weatherviewer.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Removes a session's live-update subscription (dashboard or forecast) as
 * soon as its WebSocket connection closes - tab closed, page navigated
 * away, network drop, etc. - so {@link WeatherLiveUpdateScheduler} never
 * wastes a fetch/push on a client that's no longer listening.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherSocketEventListener {

    private final WeatherSubscriptionRegistry registry;

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        registry.remove(event.getSessionId());
        log.debug("WebSocket session {} disconnected; live-update subscription removed", event.getSessionId());
    }

}
