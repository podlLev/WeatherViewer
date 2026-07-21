package com.weatherviewer.service.impl;

import com.weatherviewer.exception.InvalidTokenException;
import com.weatherviewer.model.User;
import com.weatherviewer.model.VerificationToken;
import com.weatherviewer.model.enums.TokenType;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.repository.UserRepository;
import com.weatherviewer.repository.VerificationTokenRepository;
import com.weatherviewer.service.MailService;
import com.weatherviewer.service.VerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Default {@link VerificationService} implementation.
 * <p>
 * Tokens are 32 bytes of {@link SecureRandom} output, URL-safe
 * base64-encoded — long and unguessable enough to embed directly in an
 * emailed link. Issuing a new token for a given user/purpose first
 * invalidates any earlier unused ones of that same type, so only the most
 * recently emailed link ever works.
 */
@Service
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final String baseUrl;
    private final long verificationTtlHours;
    private final long passwordResetTtlHours;

    public VerificationServiceImpl(UserRepository userRepository,
                                   VerificationTokenRepository tokenRepository,
                                   MailService mailService,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${app.base-url:http://localhost:8080}") String baseUrl,
                                   @Value("${app.verification.token-ttl-hours:24}") long verificationTtlHours,
                                   @Value("${app.password-reset.token-ttl-hours:1}") long passwordResetTtlHours) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.baseUrl = baseUrl;
        this.verificationTtlHours = verificationTtlHours;
        this.passwordResetTtlHours = passwordResetTtlHours;
    }

    @Override
    @Transactional
    public void sendVerificationEmail(User user) {
        tokenRepository.invalidateActiveTokens(user.getId(), TokenType.EMAIL_VERIFICATION);

        String rawToken = issueToken(user, TokenType.EMAIL_VERIFICATION, verificationTtlHours);

        String link = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/verify-email")
                .queryParam("token", rawToken)
                .toUriString();

        mailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), link);
    }

    @Override
    @Transactional
    public void confirmEmail(String token) {
        VerificationToken verificationToken = redeem(token, TokenType.EMAIL_VERIFICATION);

        User user = verificationToken.getUser();
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            tokenRepository.invalidateActiveTokens(user.getId(), TokenType.PASSWORD_RESET);

            String rawToken = issueToken(user, TokenType.PASSWORD_RESET, passwordResetTtlHours);
            String link = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/reset-password")
                    .queryParam("token", rawToken)
                    .toUriString();

            mailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), link);
        }, () -> log.info("Password reset requested for unregistered email={}", email));
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newRawPassword) {
        VerificationToken verificationToken = redeem(token, TokenType.PASSWORD_RESET);

        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }

    /** Generates, persists, and returns the raw (unhashed) token string to embed in the emailed link. */
    private String issueToken(User user, TokenType type, long ttlHours) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        VerificationToken verificationToken = new VerificationToken()
                .setToken(rawToken)
                .setUser(user)
                .setType(type)
                .setExpiresAt(LocalDateTime.now().plusHours(ttlHours))
                .setUsed(false);

        tokenRepository.save(verificationToken);
        return rawToken;
    }

    /** Looks up a token by value/type, validates it, and marks it used — throwing if any of that fails. */
    private VerificationToken redeem(String rawToken, TokenType type) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(rawToken, type)
                .orElseThrow(() -> new InvalidTokenException("This link is invalid."));

        if (!verificationToken.isValid()) {
            throw new InvalidTokenException("This link has expired or was already used.");
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);
        return verificationToken;
    }

}
