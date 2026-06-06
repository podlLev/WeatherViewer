package com.weatherviewer.model.enums;

import com.weatherviewer.exception.notfound.RoleNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER(Set.of(Permission.USERS_READ)),
    ADMIN(Set.of(Permission.USERS_WRITE, Permission.USERS_READ));

    private final Set<Permission> permissions;

    public Set<SimpleGrantedAuthority> getAuthority() {
        return getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
    }

    public static Role getInstance(String roleName) {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new RoleNotFoundException("Role not found by name: " + roleName);
    }

}
