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

/**
 * Post-login redirect handler.
 * <p>
 * If the request carries a {@code redirect} query parameter (e.g. the user
 * was bounced to the login page from a deep link), this handler sends them
 * there instead of the default "last requested page" — but only after
 * validating it with {@link SafeRedirectUtils#isSafeRedirect(String)} to
 * prevent open-redirect attacks. Falls back to the standard saved-request
 * behavior otherwise.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    /**
     * Redirects to a validated {@code redirect} parameter if present and
     * safe; otherwise delegates to the default saved-request redirect.
     */
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
