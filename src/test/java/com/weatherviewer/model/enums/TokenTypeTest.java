package com.weatherviewer.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenTypeTest {

    @Test
    void values_containsExactlyEmailVerificationAndPasswordReset() {
        assertThat(TokenType.values())
                .containsExactly(TokenType.EMAIL_VERIFICATION, TokenType.PASSWORD_RESET);
    }

    @Test
    void valueOf_emailVerification_returnsMatchingConstant() {
        assertThat(TokenType.valueOf("EMAIL_VERIFICATION")).isEqualTo(TokenType.EMAIL_VERIFICATION);
    }

    @Test
    void valueOf_passwordReset_returnsMatchingConstant() {
        assertThat(TokenType.valueOf("PASSWORD_RESET")).isEqualTo(TokenType.PASSWORD_RESET);
    }

    @Test
    void valueOf_unknownConstant_throwsIllegalArgumentException() {
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> TokenType.valueOf("NOT_A_TYPE"))).hasMessageContaining("NOT_A_TYPE");
    }

}
