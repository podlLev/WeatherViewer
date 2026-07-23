package com.weatherviewer.security;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountLockoutListenerTest {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    @Mock
    private UserRepository userRepository;

    private AccountLockoutListener listener;

    @BeforeEach
    void setUp() {
        listener = new AccountLockoutListener(userRepository);
        ReflectionTestUtils.setField(listener, "maxAttempts", MAX_ATTEMPTS);
        ReflectionTestUtils.setField(listener, "lockDurationMinutes", LOCK_DURATION_MINUTES);
    }

    private User user(int failedAttempts) {
        User user = (User) new User()
                .setEmail("john@example.com")
                .setPassword("hashed")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER)
                .setId(UUID.randomUUID());
        user.setFailedLoginAttempts(failedAttempts);
        return user;
    }

    @Test
    void onAuthenticationFailure_belowThreshold_incrementsWithoutLocking() {
        User user = user(1);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        listener.onAuthenticationFailure(failureEvent("john@example.com"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isEqualTo(2);
        assertThat(captor.getValue().getLockedUntil()).isNull();
    }

    @Test
    void onAuthenticationFailure_reachesThreshold_locksAccount() {
        User user = user(MAX_ATTEMPTS - 1);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        listener.onAuthenticationFailure(failureEvent("john@example.com"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isEqualTo(MAX_ATTEMPTS);
        assertThat(captor.getValue().getLockedUntil())
                .isAfter(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES).minusSeconds(5));
    }

    @Test
    void onAuthenticationFailure_unknownEmail_doesNothing() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        listener.onAuthenticationFailure(failureEvent("ghost@example.com"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void onAuthenticationSuccess_clearsCounterAndLock() {
        User user = user(3);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        listener.onAuthenticationSuccess(successEvent(user));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isZero();
        assertThat(captor.getValue().getLockedUntil()).isNull();
    }

    @Test
    void onAuthenticationSuccess_noPriorFailures_doesNotWriteToDatabase() {
        User user = user(0);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        listener.onAuthenticationSuccess(successEvent(user));

        verify(userRepository, never()).save(any());
    }

    @Test
    void onAuthenticationSuccess_principalNotSecUser_doesNothing() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        listener.onAuthenticationSuccess(event);

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void onAuthenticationSuccess_attemptsZeroButLockedUntilSet_resetsAndSaves() {
        User user = user(0);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        listener.onAuthenticationSuccess(successEvent(user));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isZero();
        assertThat(captor.getValue().getLockedUntil()).isNull();
    }

    private AuthenticationFailureBadCredentialsEvent failureEvent(String email) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, "wrong-password");
        return new AuthenticationFailureBadCredentialsEvent(authentication, new org.springframework.security.authentication.BadCredentialsException("bad credentials"));
    }

    private AuthenticationSuccessEvent successEvent(User user) {
        SecUser secUser = SecUser.fromUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(secUser, null, secUser.getAuthorities());
        return new AuthenticationSuccessEvent(authentication);
    }

}
