package com.weatherviewer.security;

import com.weatherviewer.utils.SafeRedirectUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String redirectUrl = request.getParameter("redirect");
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            if (!SafeRedirectUtils.isSafeRedirect(redirectUrl)) {
                log.warn("Rejected unsafe redirect target after login: {}", redirectUrl);
            } else {
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

}
