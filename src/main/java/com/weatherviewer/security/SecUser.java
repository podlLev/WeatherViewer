package com.weatherviewer.security;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.model.enums.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Security {@link UserDetails} adapter around this application's
 * {@link User} entity.
 * <p>
 * All four account-state checks ({@code isAccountNonExpired},
 * {@code isAccountNonLocked}, {@code isCredentialsNonExpired},
 * {@code isEnabled}) are backed by the single {@link #isActive} flag —
 * this app doesn't distinguish between those states, so any non-{@code ACTIVE}
 * {@link UserStatus} simply locks the account out of authentication.
 */
@Getter
@RequiredArgsConstructor
public class SecUser implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final Set<SimpleGrantedAuthority> authorities;
    private final Boolean isActive;
    private final String fullName;
    private final UnitSystem units;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    /**
     * Builds a {@link SecUser} principal from a {@link User} entity,
     * deriving authorities from the user's {@link com.weatherviewer.model.enums.Role}
     * and considering the account active only when its status is
     * {@link UserStatus#ACTIVE}.
     */
    public static SecUser fromUser(User user) {
        return new SecUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().getAuthority(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getFullName(),
                user.getUnits()
        );
    }

}
