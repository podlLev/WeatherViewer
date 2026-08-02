package com.weatherviewer.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WeatherSocketEventListenerTest {

    @Mock
    private WeatherSubscriptionRegistry registry;

    private WeatherSocketEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new WeatherSocketEventListener(registry);
    }

    private SessionDisconnectEvent disconnectEvent(String sessionId) {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();
        return new SessionDisconnectEvent(this, message, sessionId, CloseStatus.NORMAL);
    }

    @Test
    void onSessionDisconnect_removesSessionFromRegistry() {
        listener.onSessionDisconnect(disconnectEvent("session-1"));

        verify(registry).remove("session-1");
    }

    @Test
    void onSessionDisconnect_differentSessionIds_eachRemovedIndependently() {
        listener.onSessionDisconnect(disconnectEvent("session-1"));
        listener.onSessionDisconnect(disconnectEvent("session-2"));

        verify(registry).remove("session-1");
        verify(registry).remove("session-2");
    }

}
