package com.weatherviewer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SafeRedirectUtilsTest {

    @Nested
    @DisplayName("Tests for isSafeRedirect(String)")
    class IsSafeRedirectTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should return false for null, empty, or blank targets")
        void shouldReturnFalseForBlankTargets(String target) {
            assertFalse(SafeRedirectUtils.isSafeRedirect(target));
        }

        @Test
        @DisplayName("Should return true for a valid single-slash root path")
        void shouldReturnTrueForRootPath() {
            assertTrue(SafeRedirectUtils.isSafeRedirect("/"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/dashboard", "/profile/settings", "/home?user=test"})
        @DisplayName("Should return true for valid absolute local paths")
        void shouldReturnTrueForValidLocalPaths(String target) {
            assertTrue(SafeRedirectUtils.isSafeRedirect(target));
        }

        @ParameterizedTest
        @ValueSource(strings = {"  /dashboard  ", "\t/home\n"})
        @DisplayName("Should return true for valid paths wrapped in leading/trailing whitespace")
        void shouldReturnTrueForValidPathsWithWhitespace(String target) {
            assertTrue(SafeRedirectUtils.isSafeRedirect(target));
        }

        @ParameterizedTest
        @ValueSource(strings = {"https://malicious.com", "https://google.com", "ftp://unsafe.com"})
        @DisplayName("Should return false for targets containing a protocol scheme separator (':')")
        void shouldReturnFalseForExternalUrlsWithColon(String target) {
            assertFalse(SafeRedirectUtils.isSafeRedirect(target));
        }

        @ParameterizedTest
        @ValueSource(strings = {"dashboard", "profile/settings", "home"})
        @DisplayName("Should return false for relative paths not starting with a slash")
        void shouldReturnFalseForRelativePathsWithoutSlash(String target) {
            assertFalse(SafeRedirectUtils.isSafeRedirect(target));
        }

        @ParameterizedTest
        @ValueSource(strings = {"//attacker.com", "///evil.com"})
        @DisplayName("Should return false for protocol-relative URLs starting with '//'")
        void shouldReturnFalseForProtocolRelativeUrls(String target) {
            assertFalse(SafeRedirectUtils.isSafeRedirect(target));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/unsafe\\path", "/path\\with\\backslashes"})
        @DisplayName("Should return false for paths containing backslashes")
        void shouldReturnFalseForPathsWithBackslashes(String target) {
            assertFalse(SafeRedirectUtils.isSafeRedirect(target));
        }

        @Test
        @DisplayName("Should return false if the path contains ISO control characters")
        void shouldReturnFalseForIsoControlCharacters() {
            String targetWithControlChar = "/dashboard\u0000/settings";
            assertFalse(SafeRedirectUtils.isSafeRedirect(targetWithControlChar));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/page:8080", "/javascript:alert(1)", "/path/to:something"})
        @DisplayName("Should return false for paths that start with a slash but contain a colon later")
        void shouldReturnFalseForPathsWithColon(String target) {
            assertFalse(SafeRedirectUtils.isSafeRedirect(target));
        }

    }

    @Nested
    @DisplayName("Tests for sanitize(String, String)")
    class SanitizeTests {

        private final String FALLBACK = "/dashboard";

        @Test
        @DisplayName("Should return trimmed target when target is safe")
        void shouldReturnTrimmedTargetWhenSafe() {
            String safeTarget = "  /profile/settings  ";
            assertEquals("/profile/settings", SafeRedirectUtils.sanitize(safeTarget, FALLBACK));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"https://evil.com", "//attacker.com", "invalid-path"})
        @DisplayName("Should return fallback when target is unsafe or blank")
        void shouldReturnFallbackWhenUnsafe(String unsafeTarget) {
            assertEquals(FALLBACK, SafeRedirectUtils.sanitize(unsafeTarget, FALLBACK));
        }
    }

}