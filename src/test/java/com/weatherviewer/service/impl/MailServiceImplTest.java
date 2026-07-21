package com.weatherviewer.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    private static final String FROM_ADDRESS = "no-reply@weatherviewer.local";

    @Mock
    private JavaMailSender mailSender;

    private MailServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MailServiceImpl(mailSender, FROM_ADDRESS);
    }

    @Test
    void sendVerificationEmail_sendsMessageWithExpectedFromToAndSubject() {
        service.sendVerificationEmail("john@example.com", "John", "https://weatherviewer.local/verify-email?token=abc");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo(FROM_ADDRESS);
        assertThat(message.getTo()).containsExactly("john@example.com");
        assertThat(message.getSubject()).isEqualTo("Verify your WeatherViewer account");
    }

    @Test
    void sendVerificationEmail_bodyContainsGreetingAndLink() {
        service.sendVerificationEmail("john@example.com", "John", "https://weatherviewer.local/verify-email?token=abc");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertThat(body).contains("Hi John,");
        assertThat(body).contains("https://weatherviewer.local/verify-email?token=abc");
        assertThat(body).contains("expires in 24 hours");
    }

    @Test
    void sendPasswordResetEmail_sendsMessageWithExpectedFromToAndSubject() {
        service.sendPasswordResetEmail("jane@example.com", "Jane", "https://weatherviewer.local/reset-password?token=xyz");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo(FROM_ADDRESS);
        assertThat(message.getTo()).containsExactly("jane@example.com");
        assertThat(message.getSubject()).isEqualTo("Reset your WeatherViewer password");
    }

    @Test
    void sendPasswordResetEmail_bodyContainsGreetingAndLink() {
        service.sendPasswordResetEmail("jane@example.com", "Jane", "https://weatherviewer.local/reset-password?token=xyz");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertThat(body).contains("Hi Jane,");
        assertThat(body).contains("https://weatherviewer.local/reset-password?token=xyz");
        assertThat(body).contains("expires in 1 hour");
    }

    @Test
    void sendVerificationEmail_mailSenderThrows_exceptionIsSwallowed() {
        doThrow(new MailSendException("SMTP unreachable")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> service.sendVerificationEmail("john@example.com", "John", "https://weatherviewer.local/verify"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendPasswordResetEmail_mailSenderThrows_exceptionIsSwallowed() {
        doThrow(new MailSendException("SMTP unreachable")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> service.sendPasswordResetEmail("jane@example.com", "Jane", "https://weatherviewer.local/reset"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendVerificationEmail_genericMailException_exceptionIsSwallowed() {
        doThrow(new MailException("boom") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> service.sendVerificationEmail("john@example.com", "John", "https://weatherviewer.local/verify"))
                .doesNotThrowAnyException();
    }

}
