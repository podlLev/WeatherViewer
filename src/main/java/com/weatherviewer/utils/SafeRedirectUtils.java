package com.weatherviewer.utils;

public class SafeRedirectUtils {

    private SafeRedirectUtils() {
    }

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

    public static String sanitize(String target, String fallback) {
        return isSafeRedirect(target) ? target.trim() : fallback;
    }

}
