package com.weatherviewer.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.server.HandshakeHandler;

import java.security.Principal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    private final WebSocketConfig config = new WebSocketConfig();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerStompEndpoints_registersWsEndpointWithAuthenticationHandshakeHandler() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setHandshakeHandler(any())).thenReturn(registration);

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws");
        ArgumentCaptor<HandshakeHandler> captor = ArgumentCaptor.forClass(HandshakeHandler.class);
        verify(registration).setHandshakeHandler(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(WebSocketConfig.AuthenticationHandshakeHandler.class);
    }

    @Test
    void configureMessageBroker_enablesQueueBrokerWithAppAndUserPrefixes() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).setUserDestinationPrefix("/user");
    }

    @Test
    void determineUser_authenticatedSecurityContext_returnsAuthenticationAsPrincipal() {
        Authentication authentication = new TestingAuthenticationToken("john@example.com", "hashed", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        WebSocketConfig.AuthenticationHandshakeHandler handler = new WebSocketConfig.AuthenticationHandshakeHandler();
        Principal result = handler.determineUser(null, null, Map.of());

        assertThat(result).isSameAs(authentication);
    }

    @Test
    void determineUser_unauthenticatedSecurityContext_returnsNull() {
        Authentication authentication = new TestingAuthenticationToken("john@example.com", "hashed");
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        WebSocketConfig.AuthenticationHandshakeHandler handler = new WebSocketConfig.AuthenticationHandshakeHandler();
        Principal result = handler.determineUser(null, null, Map.of());

        assertThat(result).isNull();
    }

    @Test
    void determineUser_noAuthenticationInContext_returnsNull() {
        SecurityContextHolder.clearContext();

        WebSocketConfig.AuthenticationHandshakeHandler handler = new WebSocketConfig.AuthenticationHandshakeHandler();
        Principal result = handler.determineUser(null, null, Map.of());

        assertThat(result).isNull();
    }

}
