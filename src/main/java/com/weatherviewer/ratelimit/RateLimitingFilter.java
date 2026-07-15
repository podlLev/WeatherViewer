package com.weatherviewer.ratelimit;

import com.weatherviewer.security.SecUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.util.Comparator;

/**
 * Servlet filter that enforces per-client request rate limits, backed by
 * {@link RedisFixedWindowRateLimiter}.
 * <p>
 * Clients are identified by authenticated user ID when available, falling
 * back to client IP (honoring {@code X-Forwarded-For}) for anonymous
 * requests. The applicable limit/window is chosen by the longest matching
 * {@link RateLimitProperties.Rule#getPathPrefix()}, falling back to the
 * configured default. When the limit is exceeded, API requests receive a
 * JSON 429 response and browser requests are redirected back with a flash
 * error message. Runs first in the filter chain via {@code @Order(1)}.
 */
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String API_PATH_PREFIX = "/api";

    private final RedisFixedWindowRateLimiter rateLimiter;
    private final RateLimitProperties properties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        RateLimitProperties.Rule rule = resolveRule(path);

        String clientKey = resolveClientKey(request);
        String redisKey = "rate-limit:%s:%s".formatted(rule.getPathPrefix() == null ? "default" : rule.getPathPrefix(), clientKey);

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(redisKey, rule.getLimit(), rule.getWindowSeconds());

        response.setHeader(RATE_LIMIT_REMAINING_HEADER, String.valueOf(result.remainingRequests()));

        if (!result.allowed()) {
            log.warn("Rate limit exceeded for key={} path={} limit={}/{}s",
                    clientKey, path, rule.getLimit(), rule.getWindowSeconds());

            response.setHeader(RETRY_AFTER_HEADER, String.valueOf(result.retryAfterSeconds()));

            if (isApiRequest(request, path)) {
                writeJsonTooManyRequests(response, result.retryAfterSeconds());
            } else {
                redirectWithFriendlyMessage(request, response, path);
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Treats {@code /api/**} paths, or any request that accepts JSON, as an API request. */
    private boolean isApiRequest(HttpServletRequest request, String path) {
        if (path.startsWith(API_PATH_PREFIX)) {
            return true;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    /** Writes a minimal 429 JSON body for API clients. */
    private void writeJsonTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"Too many requests\",\"retryAfterSeconds\":%d}".formatted(retryAfterSeconds)
        );
    }

    /** Redirects browser requests back to the same path with a flash-scoped, user-friendly error message. */
    private void redirectWithFriendlyMessage(HttpServletRequest request,
                                             HttpServletResponse response,
                                             String path) {
        SessionFlashMapManager flashMapManager = new SessionFlashMapManager();
        FlashMap flashMap = new FlashMap();
        flashMap.put("errorMessage", "You're making requests too quickly. Please wait a moment and try again.");
        flashMap.setTargetRequestPath(path);
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        response.setStatus(HttpStatus.SEE_OTHER.value());
        response.setHeader("Location", path);
    }

    /** Picks the longest matching path-prefix rule for the request, or the configured default if none match. */
    private RateLimitProperties.Rule resolveRule(String path) {
        return properties.getRules().stream()
                .filter(rule -> rule.getPathPrefix() != null && path.startsWith(rule.getPathPrefix()))
                .max(Comparator.comparingInt(a -> a.getPathPrefix().length()))
                .orElseGet(this::defaultRule);
    }

    /** Builds an unnamed rule from the configured default limit/window. */
    private RateLimitProperties.Rule defaultRule() {
        RateLimitProperties.Rule fallback = new RateLimitProperties.Rule();
        fallback.setPathPrefix(null);
        fallback.setLimit(properties.getDefaultLimit());
        fallback.setWindowSeconds(properties.getDefaultWindowSeconds());
        return fallback;
    }

    /** Keys authenticated requests by user ID, anonymous requests by client IP. */
    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof SecUser secUser) {
            return "user:" + secUser.getId();
        }

        return "ip:" + resolveClientIp(request);
    }

    /** Prefers the first {@code X-Forwarded-For} entry (if present) over the raw socket address, for use behind a proxy. */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
