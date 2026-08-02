package com.weatherviewer.websocket;

import com.weatherviewer.dto.ws.DashboardSubscribeRequest;
import com.weatherviewer.dto.ws.ForecastSubscribeRequest;
import com.weatherviewer.security.SecUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Set;

/**
 * STOMP message handlers for {@code /app/**} destinations. These don't
 * return a value (no {@code @SendTo}) - registering a subscription here
 * just updates {@link WeatherSubscriptionRegistry}; the actual weather
 * pushes are sent later, out-of-band, by {@link WeatherLiveUpdateScheduler}.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WeatherSocketController {

    private static final Set<String> VALID_SORTS = Set.of("date", "nameAsc", "nameDesc", "favoriteFirst", "favoritesOnly");

    private final WeatherSubscriptionRegistry registry;

    /** Registers (or replaces) this session's live dashboard subscription for the given sort/page. */
    @MessageMapping("/dashboard.subscribe")
    public void subscribeDashboard(@Payload DashboardSubscribeRequest request, Principal principal,
                                   SimpMessageHeaderAccessor headerAccessor) {
        SecUser user = extractUser(principal);
        String sessionId = headerAccessor.getSessionId();
        if (user == null || sessionId == null) {
            log.warn("Ignoring dashboard.subscribe with no authenticated principal or session id");
            return;
        }

        String sort = request.getSort() != null && VALID_SORTS.contains(request.getSort()) ? request.getSort() : "date";
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 0;

        registry.registerDashboard(new DashboardSubscription(sessionId, user.getId(), user.getUsername(), user.getUnits(), sort, page));
        log.debug("Live dashboard subscription registered: user={}, sort={}, page={}", user.getUsername(), sort, page);
    }

    /** Registers (or replaces) this session's live forecast subscription for the given coordinates. */
    @MessageMapping("/forecast.subscribe")
    public void subscribeForecast(@Payload ForecastSubscribeRequest request, Principal principal,
                                  SimpMessageHeaderAccessor headerAccessor) {
        SecUser user = extractUser(principal);
        String sessionId = headerAccessor.getSessionId();
        if (user == null || sessionId == null || request.getLat() == null || request.getLon() == null) {
            log.warn("Ignoring forecast.subscribe with missing principal, session id, or coordinates");
            return;
        }

        registry.registerForecast(new ForecastSubscription(sessionId, user.getId(), user.getUsername(), user.getUnits(),
                request.getLat(), request.getLon()));
        log.debug("Live forecast subscription registered: user={}, lat={}, lon={}", user.getUsername(), request.getLat(), request.getLon());
    }

    private SecUser extractUser(Principal principal) {
        if (principal instanceof Authentication authentication && authentication.getPrincipal() instanceof SecUser secUser) {
            return secUser;
        }
        return null;
    }

}
