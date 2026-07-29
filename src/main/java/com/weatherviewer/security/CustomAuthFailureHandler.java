package com.weatherviewer.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Redirects failed sign-in attempts to a dedicated {@code /sign-in-failure}
 * page instead of Spring Security's default behavior of re-rendering the
 * login page with a generic query parameter.
 * <p>
 * A {@link DisabledException} means the account exists and the password
 * was correct, but {@link com.weatherviewer.security.SecUser#isEnabled()}
 * is {@code false} — in practice, an account still {@code PENDING} email
 * verification. That case is distinguished from ordinary bad-credentials
 * failures via an {@code unverified} query parameter, so the sign-in page
 * can offer to resend the verification email instead of just "try again".
 * <p>
 * A {@link LockedException} means {@link com.weatherviewer.security.SecUser#isAccountNonLocked()}
 * is {@code false} — {@link com.weatherviewer.security.AccountLockoutListener}
 * locked the account after too many recent failed attempts. That's
 * distinguished via a {@code locked} query parameter, so the sign-in page
 * can tell the user to wait rather than implying their password is wrong.
 */
@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomAuthFailureHandler() {
        super("/sign-in-failure");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof LockedException) {
            redirectWithParam(request, response, "locked");
            return;
        }

        if (!(exception instanceof DisabledException)) {
            super.onAuthenticationFailure(request, response, exception);
            return;
        }

        redirectWithParam(request, response, "unverified");
    }

    private void redirectWithParam(HttpServletRequest request, HttpServletResponse response, String param)
            throws IOException {
        UriComponentsBuilder targetUrl = UriComponentsBuilder.fromPath("/sign-in-failure")
                .queryParam(param, "true");

        String email = request.getParameter("email");
        if (email != null && !email.isBlank()) {
            targetUrl.queryParam("email", email);
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl.toUriString());
    }

}
