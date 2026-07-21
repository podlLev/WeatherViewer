package com.weatherviewer.service.impl;

import com.weatherviewer.exception.InvalidTokenException;
import com.weatherviewer.model.User;
import com.weatherviewer.model.VerificationToken;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.TokenType;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.repository.UserRepository;
import com.weatherviewer.repository.VerificationTokenRepository;
import com.weatherviewer.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

    private static final String BASE_URL = "https://weatherviewer.local";
    private static final long VERIFICATION_TTL_HOURS = 24;
    private static final long PASSWORD_RESET_TTL_HOURS = 1;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private VerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VerificationServiceImpl(userRepository, tokenRepository, mailService, passwordEncoder,
                BASE_URL, VERIFICATION_TTL_HOURS, PASSWORD_RESET_TTL_HOURS);
    }

    private User user(UUID id) {
        return (User) new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("hashed")
                .setStatus(UserStatus.PENDING)
                .setRole(Role.USER)
                .setId(id);
    }

    private VerificationToken tokenFor(User owner, TokenType type, String value, boolean used, LocalDateTime expiresAt) {
        return (VerificationToken) new VerificationToken()
                .setToken(value)
                .setType(type)
                .setUser(owner)
                .setUsed(used)
                .setExpiresAt(expiresAt)
                .setId(UUID.randomUUID());
    }

    @Test
    void sendVerificationEmail_invalidatesPriorTokensAndPersistsNewOne() {
        User user = user(UUID.randomUUID());

        service.sendVerificationEmail(user);

        verify(tokenRepository).invalidateActiveTokens(user.getId(), TokenType.EMAIL_VERIFICATION);

        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository).save(captor.capture());

        VerificationToken saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getType()).isEqualTo(TokenType.EMAIL_VERIFICATION);
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getToken()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusHours(VERIFICATION_TTL_HOURS - 1));
        assertThat(saved.getExpiresAt()).isBefore(LocalDateTime.now().plusHours(VERIFICATION_TTL_HOURS + 1));
    }

    @Test
    void sendVerificationEmail_sendsMailWithLinkContainingBaseUrlAndToken() {
        User user = user(UUID.randomUUID());

        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        service.sendVerificationEmail(user);
        verify(tokenRepository).save(tokenCaptor.capture());
        String rawToken = tokenCaptor.getValue().getToken();

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendVerificationEmail(eq("john@example.com"), eq("John"), linkCaptor.capture());

        String link = linkCaptor.getValue();
        assertThat(link).startsWith(BASE_URL + "/verify-email?token=");
        assertThat(link).contains(rawToken);
    }

    @Test
    void sendVerificationEmail_generatesDifferentTokenEachCall() {
        User user = user(UUID.randomUUID());
        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);

        service.sendVerificationEmail(user);
        service.sendVerificationEmail(user);

        verify(tokenRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getToken()).isNotEqualTo(captor.getAllValues().get(1).getToken());
    }

    @Test
    void confirmEmail_validToken_activatesPendingUserAndMarksTokenUsed() {
        User user = user(UUID.randomUUID());
        VerificationToken token = tokenFor(user, TokenType.EMAIL_VERIFICATION, "raw-token", false,
                LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenAndType("raw-token", TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));

        service.confirmEmail("raw-token");

        assertThat(token.isUsed()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    void confirmEmail_userAlreadyActive_doesNotResaveUser() {
        User user = user(UUID.randomUUID());
        user.setStatus(UserStatus.ACTIVE);
        VerificationToken token = tokenFor(user, TokenType.EMAIL_VERIFICATION, "raw-token", false,
                LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenAndType("raw-token", TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));

        service.confirmEmail("raw-token");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void confirmEmail_unknownToken_throwsInvalidTokenException() {
        when(tokenRepository.findByTokenAndType("bogus", TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmEmail("bogus"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("This link is invalid.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void confirmEmail_expiredToken_throwsInvalidTokenException() {
        User user = user(UUID.randomUUID());
        VerificationToken token = tokenFor(user, TokenType.EMAIL_VERIFICATION, "raw-token", false,
                LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenAndType("raw-token", TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmEmail("raw-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("This link has expired or was already used.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void confirmEmail_alreadyUsedToken_throwsInvalidTokenException() {
        User user = user(UUID.randomUUID());
        VerificationToken token = tokenFor(user, TokenType.EMAIL_VERIFICATION, "raw-token", true,
                LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenAndType("raw-token", TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmEmail("raw-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("This link has expired or was already used.");
    }

    @Test
    void confirmEmail_passwordResetTypeToken_notFoundAsEmailVerification() {
        when(tokenRepository.findByTokenAndType("raw-token", TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmEmail("raw-token"))
                .isInstanceOf(InvalidTokenException.class);

        verify(tokenRepository, never()).findByTokenAndType("raw-token", TokenType.PASSWORD_RESET);
    }

    @Test
    void requestPasswordReset_existingEmail_invalidatesPriorTokensAndSendsMail() {
        User user = user(UUID.randomUUID());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        service.requestPasswordReset("john@example.com");

        verify(tokenRepository).invalidateActiveTokens(user.getId(), TokenType.PASSWORD_RESET);

        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getType()).isEqualTo(TokenType.PASSWORD_RESET);
        assertThat(tokenCaptor.getValue().getExpiresAt())
                .isBefore(LocalDateTime.now().plusHours(PASSWORD_RESET_TTL_HOURS + 1));

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendPasswordResetEmail(eq("john@example.com"), eq("John"), linkCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith(BASE_URL + "/reset-password?token=");
    }

    @Test
    void requestPasswordReset_unregisteredEmail_doesNothingSilently() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        service.requestPasswordReset("nobody@example.com");

        verify(tokenRepository, never()).invalidateActiveTokens(any(UUID.class), any(TokenType.class));
        verify(tokenRepository, never()).save(any(VerificationToken.class));
        verify(mailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resetPassword_validToken_encodesAndSavesNewPasswordAndMarksTokenUsed() {
        User user = user(UUID.randomUUID());
        VerificationToken token = tokenFor(user, TokenType.PASSWORD_RESET, "raw-token", false,
                LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenAndType("raw-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewSecure1@")).thenReturn("new-hashed");

        service.resetPassword("raw-token", "NewSecure1@");

        assertThat(token.isUsed()).isTrue();
        assertThat(user.getPassword()).isEqualTo("new-hashed");
        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_unknownToken_throwsInvalidTokenExceptionAndNeverTouchesPassword() {
        when(tokenRepository.findByTokenAndType("bogus", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("bogus", "NewSecure1@"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("This link is invalid.");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_expiredToken_throwsInvalidTokenExceptionAndNeverTouchesPassword() {
        User user = user(UUID.randomUUID());
        VerificationToken token = tokenFor(user, TokenType.PASSWORD_RESET, "raw-token", false,
                LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenAndType("raw-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("raw-token", "NewSecure1@"))
                .isInstanceOf(InvalidTokenException.class);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_alreadyUsedToken_throwsInvalidTokenExceptionAndNeverTouchesPassword() {
        User user = user(UUID.randomUUID());
        VerificationToken token = tokenFor(user, TokenType.PASSWORD_RESET, "raw-token", true,
                LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenAndType("raw-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("raw-token", "NewSecure1@"))
                .isInstanceOf(InvalidTokenException.class);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

}
