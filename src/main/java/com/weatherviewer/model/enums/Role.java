package com.weatherviewer.model.enums;

import com.weatherviewer.exception.notfound.RoleNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authorization role assigned to a {@link com.weatherviewer.model.User}.
 * <p>
 * Each role carries a fixed set of {@link Permission}s, which are converted
 * to Spring Security authorities via {@link #getAuthority()}.
 */
@Getter
@RequiredArgsConstructor
public enum Role {

    /** Standard account; can read/manage its own resources. */
    USER(Set.of(Permission.USERS_READ)),
    /** Administrative account; can read and write all user resources. */
    ADMIN(Set.of(Permission.USERS_WRITE, Permission.USERS_READ));

    private final Set<Permission> permissions;

    /**
     * Converts this role's permissions into Spring Security authorities.
     *
     * @return the set of granted authorities for this role
     */
    public Set<SimpleGrantedAuthority> getAuthority() {
        return getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
    }

    /**
     * Resolves a role by name, case-insensitively.
     *
     * @param roleName the role name to look up (e.g. {@code "admin"})
     * @return the matching {@link Role}
     * @throws RoleNotFoundException if no role matches the given name
     */
    public static Role getInstance(String roleName) {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new RoleNotFoundException("Role not found by name: " + roleName);
    }

}
