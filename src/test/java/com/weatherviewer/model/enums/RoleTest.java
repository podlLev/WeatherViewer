package com.weatherviewer.model.enums;

import com.weatherviewer.exception.notfound.RoleNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void getAuthority_user_returnsReadOnly() {
        Set<SimpleGrantedAuthority> authorities = Role.USER.getAuthority();

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("users:read")));
    }

    @Test
    void getAuthority_admin_returnsReadAndWrite() {
        Set<SimpleGrantedAuthority> authorities = Role.ADMIN.getAuthority();

        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("users:read")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("users:write")));
    }

    @Test
    void getInstance_caseInsensitive_returnsRole() {
        Role role = Role.getInstance("user");
        assertEquals(Role.USER, role);
    }

    @Test
    void getInstance_upperCase_returnsRole() {
        Role role = Role.getInstance("ADMIN");
        assertEquals(Role.ADMIN, role);
    }

    @Test
    void getInstance_invalidRole_throwsRoleNotFoundException() {
        String invalidRoleName = "moderator";
        Exception exception = assertThrows(RoleNotFoundException.class, () -> Role.getInstance(invalidRoleName));

        assertEquals("Role not found by name: moderator", exception.getMessage());
    }

    @Test
    void getInstance_null_throwsRoleNotFoundException() {
        Exception exception = assertThrows(RoleNotFoundException.class, () -> Role.getInstance(null));
        assertEquals("Role not found by name: null", exception.getMessage());
    }

}
