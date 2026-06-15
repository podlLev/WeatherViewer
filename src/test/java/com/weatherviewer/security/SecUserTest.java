package com.weatherviewer.security;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecUserTest {

    private User user(UserStatus status, Role role) {
        return (User) new User()
                .setEmail("john@example.com")
                .setPassword("hashed")
                .setFirstName("John")
                .setLastName("Doe")
                .setStatus(status)
                .setRole(role)
                .setId(UUID.randomUUID());
    }

    @Test
    void fromUser_mapsAllFields() {
        User user = user(UserStatus.ACTIVE, Role.USER);
        SecUser secUser = SecUser.fromUser(user);

        assertThat(secUser.getId()).isEqualTo(user.getId());
        assertThat(secUser.getUsername()).isEqualTo("john@example.com");
        assertThat(secUser.getPassword()).isEqualTo("hashed");
        assertThat(secUser.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void fromUser_activeStatus_isEnabled() {
        SecUser secUser = SecUser.fromUser(user(UserStatus.ACTIVE, Role.USER));

        assertThat(secUser.isEnabled()).isTrue();
        assertThat(secUser.isAccountNonExpired()).isTrue();
        assertThat(secUser.isAccountNonLocked()).isTrue();
        assertThat(secUser.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void fromUser_inactiveStatus_isDisabled() {
        SecUser secUser = SecUser.fromUser(user(UserStatus.PENDING, Role.USER));

        assertThat(secUser.isEnabled()).isFalse();
        assertThat(secUser.isAccountNonExpired()).isFalse();
        assertThat(secUser.isAccountNonLocked()).isFalse();
        assertThat(secUser.isCredentialsNonExpired()).isFalse();
    }

    @Test
    void fromUser_userRole_hasReadAuthority() {
        SecUser secUser = SecUser.fromUser(user(UserStatus.ACTIVE, Role.USER));

        assertThat(secUser.getAuthorities())
                .extracting("authority")
                .contains("users:read");
    }

    @Test
    void fromUser_adminRole_hasReadAndWriteAuthority() {
        SecUser secUser = SecUser.fromUser(user(UserStatus.ACTIVE, Role.ADMIN));

        assertThat(secUser.getAuthorities())
                .extracting("authority")
                .contains("users:read", "users:write");
    }

    @Test
    void fromUser_userRole_doesNotHaveWriteAuthority() {
        SecUser secUser = SecUser.fromUser(user(UserStatus.ACTIVE, Role.USER));

        assertThat(secUser.getAuthorities())
                .extracting("authority")
                .doesNotContain("users:write");
    }

}
