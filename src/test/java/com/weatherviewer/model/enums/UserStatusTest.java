package com.weatherviewer.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserStatusTest {

    @Test
    void pending_hasCorrectDescription() {
        assertThat(UserStatus.PENDING.getDescription()).isEqualTo("User has not been activated yet");
    }

    @Test
    void active_hasCorrectDescription() {
        assertThat(UserStatus.ACTIVE.getDescription()).isEqualTo("User is activated and can use the system");
    }

    @Test
    void blocked_hasCorrectDescription() {
        assertThat(UserStatus.BLOCKED.getDescription()).isEqualTo("User has been blocked by the system or an administrator");
    }

    @Test
    void deleted_hasCorrectDescription() {
        assertThat(UserStatus.DELETED.getDescription()).isEqualTo("User account has been deleted (soft delete)");
    }

    @Test
    void expired_hasCorrectDescription() {
        assertThat(UserStatus.EXPIRED.getDescription()).isEqualTo("User account is expired and needs reactivation");
    }

    @Test
    void values_hasFiveStatuses() {
        assertThat(UserStatus.values()).hasSize(5);
    }

}
