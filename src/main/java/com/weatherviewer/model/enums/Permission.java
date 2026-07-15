package com.weatherviewer.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Fine-grained permissions that back Spring Security authorities.
 * <p>
 * Each {@link com.weatherviewer.model.enums.Role} maps to a set of these
 * permissions; the string value is what's registered as a
 * {@link org.springframework.security.core.authority.SimpleGrantedAuthority}.
 */
@Getter
@RequiredArgsConstructor
public enum Permission {

    /** Grants read access to user resources (e.g. viewing profiles). */
    USERS_READ("users:read"),
    /** Grants write access to user resources (e.g. editing/deleting accounts, changing roles). */
    USERS_WRITE("users:write");

    private final String permission;

}
