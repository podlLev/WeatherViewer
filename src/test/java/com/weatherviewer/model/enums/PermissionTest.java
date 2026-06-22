package com.weatherviewer.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionTest {

    @Test
    void usersRead_hasCorrectPermissionString() {
        assertThat(Permission.USERS_READ.getPermission()).isEqualTo("users:read");
    }

    @Test
    void usersWrite_hasCorrectPermissionString() {
        assertThat(Permission.USERS_WRITE.getPermission()).isEqualTo("users:write");
    }

    @Test
    void values_hasTwoPermissions() {
        assertThat(Permission.values()).hasSize(2);
    }

}
