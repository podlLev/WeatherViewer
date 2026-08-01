package com.weatherviewer.config;

import com.weatherviewer.websocket.WeatherSocketController;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Wires up STOMP-over-WebSocket messaging for live weather updates.
 * <p>
 * The app stays on plain (non-SockJS) WebSocket: every browser this app
 * targets supports it natively, and skipping SockJS avoids its XHR-polling
 * fallback transports, which are same-origin POSTs that would otherwise
 * need a CSRF-exemption carve-out in {@link com.weatherviewer.config.SecurityConfig}.
 * The handshake itself is a plain {@code GET} on {@code /ws} and is subject
 * to the app's normal {@code anyRequest().authenticated()} rule, so only a
 * signed-in session can open the socket in the first place.
 * <p>
 * Destination layout:
 * <ul>
 *     <li>{@code /app/**} - client-to-server, handled by {@link WeatherSocketController}</li>
 *     <li>{@code /user/queue/dashboard} - server-to-client, one user's dashboard weather</li>
 *     <li>{@code /user/queue/forecast} - server-to-client, one user's forecast-page weather</li>
 * </ul>
 * Both queues are per-user (not broadcast topics): weather is unit-converted
 * per viewer and dashboard contents are private to their owner, so a shared
 * {@code /topic/**} broadcast isn't the right shape here.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new AuthenticationHandshakeHandler());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Copies the {@link Authentication} already established for the
     * handshake HTTP request (loaded from the session by Spring Security's
     * filter chain, same as any other authenticated request) onto the
     * WebSocket session as its {@link Principal}. Without this, every STOMP
     * session would be anonymous and {@code convertAndSendToUser} would have
     * no username to route on.
     */
    static class AuthenticationHandshakeHandler extends DefaultHandshakeHandler {

        @Override
        protected Principal determineUser(@NonNull ServerHttpRequest request,
                                          @NonNull WebSocketHandler wsHandler,
                                          @NonNull Map<String, Object> attributes) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            return authentication;
        }
    }

}
