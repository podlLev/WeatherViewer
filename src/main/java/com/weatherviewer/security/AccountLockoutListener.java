package com.weatherviewer.security;

import com.weatherviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Tracks consecutive failed sign-in attempts per account and applies a
 * temporary lockout once a threshold is reached, mitigating credential
 * stuffing / brute-force attempts against a single known email address —
 * something IP- or session-based rate limiting alone doesn't fully cover,
 * since an attacker can spread attempts across many IPs.
 * <p>
 * Listens to the {@link AuthenticationFailureBadCredentialsEvent} and
 * {@link AuthenticationSuccessEvent} that Spring Security's default
 * {@code AuthenticationEventPublisher} raises around every
 * {@code DaoAuthenticationProvider} attempt. Once an account is locked,
 * {@link SecUser#isAccountNonLocked()} makes subsequent attempts fail with
 * {@code LockedException} instead of {@code BadCredentialsException} — so
 * this listener naturally stops incrementing further while the lock is in
 * effect, rather than perpetually extending it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountLockoutListener {

    private final UserRepository userRepository;

    /** Consecutive failures before an account is locked. */
    @Value("${security.account-lockout.max-attempts:5}")
    private int maxAttempts;

    /** How long an account stays locked once the threshold is hit. */
    @Value("${security.account-lockout.lock-duration-minutes:15}")
    private long lockDurationMinutes;

    /**
     * On a failed login with valid credentials-format-but-wrong-password
     * (or, thanks to {@code hideUserNotFoundExceptions}, an unknown email
     * too — though in that case there's no matching row to update),
     * increments the account's failure counter and locks it once
     * {@link #maxAttempts} is reached.
     */
    @EventListener
    @Transactional
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getName();
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= maxAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
                log.warn("Account locked for {} minutes after {} consecutive failed sign-in attempts: {}",
                        lockDurationMinutes, attempts, email);
            } else {
                log.debug("Failed sign-in attempt {}/{} for {}", attempts, maxAttempts, email);
            }

            userRepository.save(user);
        });
    }

    /**
     * Clears the failure counter and any active lockout on a successful
     * sign-in, so a legitimate user who mistyped their password a few
     * times isn't left partway toward a lockout on their next visit.
     */
    @EventListener
    @Transactional
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (!(event.getAuthentication().getPrincipal() instanceof SecUser secUser)) {
            return;
        }

        userRepository.findById(secUser.getId()).ifPresent(user -> {
            if (user.getFailedLoginAttempts() != 0 || user.getLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }
        });
    }

}
