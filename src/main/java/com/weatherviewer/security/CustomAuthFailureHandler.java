package com.weatherviewer.security;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Redirects failed sign-in attempts to a dedicated {@code /sign-in-failure}
 * page instead of Spring Security's default behavior of re-rendering the
 * login page with a generic query parameter.
 */
@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomAuthFailureHandler() {
        super("/sign-in-failure");
    }

}
