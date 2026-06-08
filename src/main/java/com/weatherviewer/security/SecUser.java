package com.weatherviewer.security;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class SecUser implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final Set<SimpleGrantedAuthority> authorities;
    private final Boolean isActive;
    private final String fullName;

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

    public static SecUser fromUser(User user) {
        return new SecUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().getAuthority(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getFullName()
        );
    }

}
