package com.weatherviewer.ratelimit;

import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.security.SecUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private RedisFixedWindowRateLimiter rateLimiter;

    @Mock
    private FilterChain filterChain;

    private RateLimitProperties properties;
    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setDefaultLimit(100);
        properties.setDefaultWindowSeconds(60);
        filter = new RateLimitingFilter(rateLimiter, properties);
        filter.initTrustedProxies();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private RateLimitProperties.Rule rule(String prefix, int limit) {
        RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
        rule.setPathPrefix(prefix);
        rule.setLimit(limit);
        rule.setWindowSeconds(60);
        return rule;
    }

    private void authenticateAs(UUID userId) {
        SecUser secUser = new SecUser(
                userId,
                "john@example.com",
                "hashed",
                java.util.Set.of(),
                true,
                "John Doe",
                UnitSystem.METRIC,
                null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(secUser, null, secUser.getAuthorities()));
    }

    @Test
    void disabled_passesRequestThroughWithoutTouchingLimiter() throws Exception {
        properties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(rateLimiter);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void allowed_passesRequestToFilterChain() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 9, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void allowed_setsRemainingHeader() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 9, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("9");
    }

    @Test
    void blocked_apiPath_returnsJsonBody() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 30));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/weather/city");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentAsString()).isEqualTo("{\"error\":\"Too many requests\",\"retryAfterSeconds\":30}");
    }

    @Test
    void blocked_apiPath_returns429Status() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 30));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/weather/city");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void blocked_apiPath_setsContentTypeJson() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 30));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/weather/city");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentType()).contains("application/json");
    }

    @Test
    void blocked_apiPath_setsRetryAfterHeader() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 30));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/weather/city");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("Retry-After")).isEqualTo("30");
    }

    @Test
    void blocked_apiPath_doesNotContinueFilterChain() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 30));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/weather/city");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void blocked_nonApiPathWithJsonAcceptHeader_isTreatedAsApiRequest() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 15));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sign-in");
        request.addHeader("Accept", "application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void blocked_browserSignIn_redirectsBackToSamePath() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 47));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sign-in");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getHeader("Location")).isEqualTo("/sign-in");
    }

    @Test
    void blocked_browserSignIn_doesNotWriteJsonBody() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 47));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sign-in");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    void blocked_browserSignIn_setsRetryAfterHeader() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 47));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sign-in");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("Retry-After")).isEqualTo("47");
    }

    @Test
    void blocked_browserSignIn_doesNotContinueFilterChain() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 47));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sign-in");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void resolveRule_usesMostSpecificMatchingPrefix() throws Exception {
        properties.setRules(List.of(
                rule("/api", 1000),
                rule("/api/v1/weather", 5)
        ));
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/weather/city");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(anyString(), org.mockito.ArgumentMatchers.eq(5), org.mockito.ArgumentMatchers.eq(60));
    }

    @Test
    void resolveRule_noMatchingPrefix_fallsBackToDefaultLimits() throws Exception {
        properties.setDefaultLimit(42);
        properties.setDefaultWindowSeconds(7);
        properties.setRules(List.of(rule("/sign-in", 10)));
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 7));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/some/other/path");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(anyString(), org.mockito.ArgumentMatchers.eq(42), org.mockito.ArgumentMatchers.eq(7));
    }

    @Test
    void clientKey_anonymousRequest_isKeyedByRemoteAddress() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("203.0.113.5");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("ip:203.0.113.5"), anyInt(), anyInt());
    }

    @Test
    void clientKey_xForwardedForHeader_trustedProxy_takesPrecedenceOverRemoteAddress() throws Exception {
        properties.setTrustedProxies(List.of("10.0.0.1"));
        filter.initTrustedProxies();

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "198.51.100.7, 10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("ip:198.51.100.7"), anyInt(), anyInt());
    }

    @Test
    void clientKey_xForwardedForHeader_untrustedRemoteAddr_isIgnored() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("203.0.113.5");
        request.addHeader("X-Forwarded-For", "198.51.100.7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("ip:203.0.113.5"), anyInt(), anyInt());
        verify(rateLimiter, never()).tryConsume(org.mockito.ArgumentMatchers.contains("198.51.100.7"), anyInt(), anyInt());
    }

    @Test
    void clientKey_xForwardedForHeader_remoteAddrOutsideConfiguredCidr_isIgnored() throws Exception {
        properties.setTrustedProxies(List.of("10.0.0.0/24"));
        filter.initTrustedProxies();

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("11.0.0.5");
        request.addHeader("X-Forwarded-For", "198.51.100.7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("ip:11.0.0.5"), anyInt(), anyInt());
    }

    @Test
    void clientKey_xForwardedForHeader_remoteAddrInsideConfiguredCidr_isHonored() throws Exception {
        properties.setTrustedProxies(List.of("10.0.0.0/24"));
        filter.initTrustedProxies();

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("10.0.0.42");
        request.addHeader("X-Forwarded-For", "198.51.100.7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("ip:198.51.100.7"), anyInt(), anyInt());
    }

    @Test
    void clientKey_multipleConfiguredTrustedProxies_matchesAnyOfThem() throws Exception {
        properties.setTrustedProxies(List.of("192.168.1.1", "10.0.0.0/8"));
        filter.initTrustedProxies();

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("10.20.30.40");
        request.addHeader("X-Forwarded-For", "198.51.100.7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("ip:198.51.100.7"), anyInt(), anyInt());
    }

    @Test
    void clientKey_authenticatedRequest_isKeyedByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        authenticateAs(userId);
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/locations/my");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("user:" + userId), anyInt(), anyInt());
    }

    @Test
    void clientKey_differentUsersBehindSameIp_getIndependentKeys() throws Exception {
        UUID firstUser = UUID.randomUUID();
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));

        authenticateAs(firstUser);
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v1/locations/my");
        firstRequest.setRemoteAddr("203.0.113.5");
        filter.doFilterInternal(firstRequest, new MockHttpServletResponse(), filterChain);

        SecurityContextHolder.clearContext();
        UUID secondUser = UUID.randomUUID();
        authenticateAs(secondUser);
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v1/locations/my");
        secondRequest.setRemoteAddr("203.0.113.5");
        filter.doFilterInternal(secondRequest, new MockHttpServletResponse(), filterChain);

        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("user:" + firstUser), anyInt(), anyInt());
        verify(rateLimiter).tryConsume(org.mockito.ArgumentMatchers.contains("user:" + secondUser), anyInt(), anyInt());
    }

    @Test
    void blocked_nonApiRequestWithHtmlAccept_redirectsInsteadOfReturningJson() throws Exception {
        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(false, 0, 15));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sign-in");
        request.addHeader("Accept", "text/html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getHeader("Location")).isEqualTo("/sign-in");
    }

    @Test
    void clientKey_trustedProxyWithNoForwardedForHeader_fallsBackToRemoteAddress() throws Exception {
        properties.setTrustedProxies(List.of("203.0.113.9"));
        filter.initTrustedProxies();

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("203.0.113.9");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(rateLimiter).tryConsume(contains("ip:203.0.113.9"), anyInt(), anyInt());
    }

    @Test
    void clientKey_blankForwardedForFallsBackToRemoteAddress() throws Exception {
        properties.setTrustedProxies(List.of("203.0.113.9"));
        filter.initTrustedProxies();

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.addHeader("X-Forwarded-For", "   ");
        request.setRemoteAddr("203.0.113.9");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(rateLimiter)
                .tryConsume(contains("ip:203.0.113.9"), anyInt(), anyInt());
    }

    @Test
    void clientKey_notAuthenticatedFallsBackToIp() throws Exception {
        var authentication =
                new UsernamePasswordAuthenticationToken("anonymous", null);
        authentication.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("192.168.1.15");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(rateLimiter)
                .tryConsume(contains("ip:192.168.1.15"), anyInt(), anyInt());
    }

    @Test
    void clientKey_authenticatedPrincipalNotSecUserFallsBackToIp() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "plain-user",
                        null,
                        List.of()
                )
        );

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 0, 60));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sign-in");
        request.setRemoteAddr("198.51.100.20");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(rateLimiter)
                .tryConsume(contains("ip:198.51.100.20"), anyInt(), anyInt());
    }

    @Test
    void doFilter_withMultipleMatchingRules_shouldPickTheMostSpecificLongestPrefixRule() throws Exception {
        RateLimitProperties.Rule generalApiRule = new RateLimitProperties.Rule();
        generalApiRule.setPathPrefix("/api");
        generalApiRule.setLimit(100);
        generalApiRule.setWindowSeconds(60);

        RateLimitProperties.Rule specificApiRule = new RateLimitProperties.Rule();
        specificApiRule.setPathPrefix("/api/v1/weather");
        specificApiRule.setLimit(5);
        specificApiRule.setWindowSeconds(10);

        properties.setRules(List.of(generalApiRule, specificApiRule));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/weather/Odesa");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        String expectedKey = "rate-limit:/api/v1/weather:ip:127.0.0.1";

        when(rateLimiter.tryConsume(eq(expectedKey), eq(5), eq(10)))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 4, 10));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("4");

        verify(rateLimiter).tryConsume(eq(expectedKey), eq(5), eq(10));
    }

    @Test
    void doFilter_withNullPathPrefixInRules_shouldIgnoreItAndFallbackToDefaultRule() throws Exception {
        RateLimitProperties.Rule invalidRule = new RateLimitProperties.Rule();
        invalidRule.setPathPrefix(null);
        invalidRule.setLimit(500);
        invalidRule.setWindowSeconds(300);

        properties.setRules(List.of(invalidRule));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/forecast");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        String expectedKey = "rate-limit:default:ip:127.0.0.1";

        when(rateLimiter.tryConsume(
                eq(expectedKey),
                eq(properties.getDefaultLimit()),
                eq(properties.getDefaultWindowSeconds())))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 9, 60));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("9");

        verify(rateLimiter).tryConsume(
                eq(expectedKey),
                eq(properties.getDefaultLimit()),
                eq(properties.getDefaultWindowSeconds()));
    }

    @Test
    void shouldNotFilter_nullPath_returnsFalse() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(null);

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 10, 0));

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(rateLimiter).tryConsume(anyString(), anyInt(), anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_noMatches_coversFalseBranch() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/weather");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(rateLimiter.tryConsume(anyString(), anyInt(), anyInt()))
                .thenReturn(new RedisFixedWindowRateLimiter.RateLimitResult(true, 10, 0));

        filter.doFilter(request, response, filterChain);

        verify(rateLimiter).tryConsume(anyString(), anyInt(), anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_matchingExtensionWithNonStaticPrefix_coversSecondBranch() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/logo.png");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(rateLimiter);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_prefixMatch_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/images/icon");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_extensionMatchOnly_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/icon.svg");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_noMatch_returnsFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/weather");
        assertFalse(filter.shouldNotFilter(request));
    }

}
