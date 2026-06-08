package com.weatherviewer.security;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomAuthFailureHandler() {
        super("/sign-in-failure");
    }

}
