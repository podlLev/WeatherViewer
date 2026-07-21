package com.weatherviewer.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
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
 */
@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomAuthFailureHandler() {
        super("/sign-in-failure");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (!(exception instanceof DisabledException)) {
            super.onAuthenticationFailure(request, response, exception);
            return;
        }

        UriComponentsBuilder targetUrl = UriComponentsBuilder.fromPath("/sign-in-failure")
                .queryParam("unverified", "true");

        String email = request.getParameter("email");
        if (email != null && !email.isBlank()) {
            targetUrl.queryParam("email", email);
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl.toUriString());
    }

}
