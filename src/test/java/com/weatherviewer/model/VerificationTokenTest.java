package com.weatherviewer.model;

import com.weatherviewer.model.enums.TokenType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationTokenTest {

    private VerificationToken token(boolean used, LocalDateTime expiresAt) {
        return new VerificationToken()
                .setToken("raw-token")
                .setType(TokenType.EMAIL_VERIFICATION)
                .setUsed(used)
                .setExpiresAt(expiresAt);
    }

    @Test
    void isValid_notUsedAndNotExpired_returnsTrue() {
        VerificationToken token = token(false, LocalDateTime.now().plusHours(1));

        assertTrue(token.isValid());
    }

    @Test
    void isValid_usedButNotExpired_returnsFalse() {
        VerificationToken token = token(true, LocalDateTime.now().plusHours(1));

        assertFalse(token.isValid());
    }

    @Test
    void isValid_notUsedButExpired_returnsFalse() {
        VerificationToken token = token(false, LocalDateTime.now().minusHours(1));

        assertFalse(token.isValid());
    }

    @Test
    void isValid_usedAndExpired_returnsFalse() {
        VerificationToken token = token(true, LocalDateTime.now().minusHours(1));

        assertFalse(token.isValid());
    }

    @Test
    void isValid_expiresExactlyNow_returnsFalse() {
        LocalDateTime now = LocalDateTime.now();
        VerificationToken token = token(false, now);

        assertFalse(token.isValid());
    }

}
