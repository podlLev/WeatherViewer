package com.weatherviewer.utils;

/**
 * Guards against open-redirect vulnerabilities when a redirect target comes
 * from user input (e.g. a {@code ?redirect=} query parameter after login).
 * Used by {@link com.weatherviewer.security.CustomAuthSuccessHandler}.
 */
public class SafeRedirectUtils {

    private SafeRedirectUtils() {
    }

    /**
     * A redirect target is considered safe only if it's a same-site,
     * root-relative path: it must start with a single {@code /} (not
     * {@code //}, which browsers treat as protocol-relative and can point
     * off-site), must not contain a backslash or colon (both can be used to
     * smuggle an absolute/off-site URL past naive checks), and must not
     * contain control characters.
     *
     * @param target the candidate redirect URL
     * @return {@code true} if {@code target} is a safe, same-site path
     */
    public static boolean isSafeRedirect(String target) {
        if (target == null || target.isBlank()) {
            return false;
        }

        String trimmed = target.trim();

        if (trimmed.charAt(0) != '/') {
            return false;
        }
        if (trimmed.length() > 1 && trimmed.charAt(1) == '/') {
            return false;
        }
        if (trimmed.contains("\\")) {
            return false;
        }
        if (trimmed.contains(":")) {
            return false;
        }
        return trimmed.chars().noneMatch(Character::isISOControl);
    }

    /**
     * Returns {@code target} trimmed if it passes {@link #isSafeRedirect},
     * otherwise returns {@code fallback}.
     */
    public static String sanitize(String target, String fallback) {
        return isSafeRedirect(target) ? target.trim() : fallback;
    }

}
