package com.weatherviewer.security;

import com.weatherviewer.exception.notfound.UserNotFoundException;
import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsServiceImpl;

    private User user() {
        return (User) new User()
                .setEmail("john@example.com")
                .setPassword("hashed")
                .setFirstName("John")
                .setLastName("Doe")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER)
                .setId(UUID.randomUUID());
    }

    @Test
    void loadUserByUsername_returnsUserDetails() {
        User user = user();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsServiceImpl.loadUserByUsername("john@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john@example.com");
    }

    @Test
    void loadUserByUsername_notFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsServiceImpl.loadUserByUsername("nobody@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("USER NOT FOUND");
    }

    @Test
    void loadUserByUsername_returnsCorrectPassword() {
        User user = user();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsServiceImpl.loadUserByUsername("john@example.com");

        assertThat(result.getPassword()).isEqualTo("hashed");
    }

    @Test
    void loadUserByUsername_returnsAuthorities() {
        User user = user();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsServiceImpl.loadUserByUsername("john@example.com");

        assertThat(result.getAuthorities()).isNotEmpty();
    }

}
