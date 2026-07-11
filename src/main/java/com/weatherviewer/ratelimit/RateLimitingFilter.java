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

@RequiredArgsConstructor
@Slf4j
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String API_PATH_PREFIX = "/api";

    private final Bucket4jRedisRateLimiter rateLimiter;
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

        Bucket4jRedisRateLimiter.RateLimitResult result =
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

    private boolean isApiRequest(HttpServletRequest request, String path) {
        if (path.startsWith(API_PATH_PREFIX)) {
            return true;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    private void writeJsonTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"Too many requests\",\"retryAfterSeconds\":%d}".formatted(retryAfterSeconds)
        );
    }

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

    private RateLimitProperties.Rule resolveRule(String path) {
        return properties.getRules().stream()
                .filter(rule -> rule.getPathPrefix() != null && path.startsWith(rule.getPathPrefix()))
                .max(Comparator.comparingInt(a -> a.getPathPrefix().length()))
                .orElseGet(this::defaultRule);
    }

    private RateLimitProperties.Rule defaultRule() {
        RateLimitProperties.Rule fallback = new RateLimitProperties.Rule();
        fallback.setPathPrefix(null);
        fallback.setLimit(properties.getDefaultLimit());
        fallback.setWindowSeconds(properties.getDefaultWindowSeconds());
        return fallback;
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof SecUser secUser) {
            return "user:" + secUser.getId();
        }

        return "ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
