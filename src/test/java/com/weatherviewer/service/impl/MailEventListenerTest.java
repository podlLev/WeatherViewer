package com.weatherviewer.service.impl;

import com.weatherviewer.event.PasswordResetEmailRequestedEvent;
import com.weatherviewer.event.VerificationEmailRequestedEvent;
import com.weatherviewer.service.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * These tests call the listener methods directly (as the {@code @Async} /
 * {@code @TransactionalEventListener} machinery only applies through a live
 * Spring context) — the behavior worth unit-testing here is simply "each
 * event is unpacked into the right {@link MailService} call".
 */
@ExtendWith(MockitoExtension.class)
class MailEventListenerTest {

    @Mock
    private MailService mailService;

    @InjectMocks
    private MailEventListener listener;

    @Test
    void onVerificationEmailRequested_delegatesToMailService() {
        VerificationEmailRequestedEvent event = new VerificationEmailRequestedEvent(
                "john@example.com", "John", "https://weatherviewer.local/verify-email?token=abc");

        listener.onVerificationEmailRequested(event);

        verify(mailService).sendVerificationEmail(
                "john@example.com", "John", "https://weatherviewer.local/verify-email?token=abc");
        verifyNoMoreInteractions(mailService);
    }

    @Test
    void onPasswordResetEmailRequested_delegatesToMailService() {
        PasswordResetEmailRequestedEvent event = new PasswordResetEmailRequestedEvent(
                "jane@example.com", "Jane", "https://weatherviewer.local/reset-password?token=xyz");

        listener.onPasswordResetEmailRequested(event);

        verify(mailService).sendPasswordResetEmail(
                "jane@example.com", "Jane", "https://weatherviewer.local/reset-password?token=xyz");
        verifyNoMoreInteractions(mailService);
    }

}
