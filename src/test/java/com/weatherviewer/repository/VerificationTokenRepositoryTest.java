package com.weatherviewer.repository;

import com.weatherviewer.model.User;
import com.weatherviewer.model.VerificationToken;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.TokenType;
import com.weatherviewer.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VerificationTokenRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    VerificationTokenRepository tokenRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = entityManager.persistAndFlush(new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("password")
                .setStatus(UserStatus.PENDING)
                .setRole(Role.USER));

        otherUser = entityManager.persistAndFlush(new User()
                .setEmail("jane@example.com")
                .setFirstName("Jane")
                .setLastName("Doe")
                .setPassword("password")
                .setStatus(UserStatus.PENDING)
                .setRole(Role.USER));
    }

    private VerificationToken token(String value, TokenType type, User owner, boolean used, LocalDateTime expiresAt) {
        return new VerificationToken()
                .setToken(value)
                .setType(type)
                .setUser(owner)
                .setUsed(used)
                .setExpiresAt(expiresAt);
    }

    @Test
    void findByTokenAndType_returnsTokenWhenExists() {
        VerificationToken saved = entityManager.persistAndFlush(
                token("abc123", TokenType.EMAIL_VERIFICATION, user, false, LocalDateTime.now().plusHours(24)));

        Optional<VerificationToken> result = tokenRepository.findByTokenAndType("abc123", TokenType.EMAIL_VERIFICATION);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(saved);
    }

    @Test
    void findByTokenAndType_wrongType_returnsEmpty() {
        entityManager.persistAndFlush(
                token("abc123", TokenType.EMAIL_VERIFICATION, user, false, LocalDateTime.now().plusHours(24)));

        Optional<VerificationToken> result = tokenRepository.findByTokenAndType("abc123", TokenType.PASSWORD_RESET);

        assertThat(result).isEmpty();
    }

    @Test
    void findByTokenAndType_unknownToken_returnsEmpty() {
        Optional<VerificationToken> result = tokenRepository.findByTokenAndType("does-not-exist", TokenType.EMAIL_VERIFICATION);

        assertThat(result).isEmpty();
    }

    @Test
    void invalidateActiveTokens_marksOnlyMatchingUserAndTypeAsUsed() {
        VerificationToken target = entityManager.persistAndFlush(
                token("target-token", TokenType.EMAIL_VERIFICATION, user, false, LocalDateTime.now().plusHours(24)));
        entityManager.persistAndFlush(
                token("other-type-token", TokenType.PASSWORD_RESET, user, false, LocalDateTime.now().plusHours(24)));
        entityManager.persistAndFlush(
                token("other-user-token", TokenType.EMAIL_VERIFICATION, otherUser, false, LocalDateTime.now().plusHours(24)));

        tokenRepository.invalidateActiveTokens(user.getId(), TokenType.EMAIL_VERIFICATION);
        entityManager.clear();

        VerificationToken reloaded = tokenRepository.findById(target.getId()).orElseThrow();
        assertThat(reloaded.isUsed()).isTrue();

        VerificationToken otherType = tokenRepository.findByTokenAndType("other-type-token", TokenType.PASSWORD_RESET).orElseThrow();
        assertThat(otherType.isUsed()).isFalse();

        VerificationToken otherUserToken = tokenRepository.findByTokenAndType("other-user-token", TokenType.EMAIL_VERIFICATION).orElseThrow();
        assertThat(otherUserToken.isUsed()).isFalse();
    }

    @Test
    void invalidateActiveTokens_alreadyUsedToken_isLeftUnchanged() {
        VerificationToken used = entityManager.persistAndFlush(
                token("already-used", TokenType.EMAIL_VERIFICATION, user, true, LocalDateTime.now().plusHours(24)));

        tokenRepository.invalidateActiveTokens(user.getId(), TokenType.EMAIL_VERIFICATION);
        entityManager.clear();

        VerificationToken reloaded = tokenRepository.findById(used.getId()).orElseThrow();
        assertThat(reloaded.isUsed()).isTrue();
    }

    @Test
    void save_duplicateTokenValue_violatesUniqueConstraint() {
        entityManager.persistAndFlush(
                token("duplicate", TokenType.EMAIL_VERIFICATION, user, false, LocalDateTime.now().plusHours(24)));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                entityManager.persistAndFlush(
                        token("duplicate", TokenType.PASSWORD_RESET, otherUser, false, LocalDateTime.now().plusHours(1))));
    }

}
