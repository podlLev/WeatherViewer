package com.weatherviewer.config;

import com.weatherviewer.ratelimit.RateLimitingFilter;
import com.weatherviewer.security.CustomAuthFailureHandler;
import com.weatherviewer.security.CustomAuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Core Spring Security configuration.
 * <p>
 * Sets up cookie-based form login (email/password against
 * {@link com.weatherviewer.security.UserDetailsServiceImpl}), CSRF
 * protection via a readable {@code XSRF-TOKEN} cookie, session-based
 * logout, and the app's public vs. authenticated URL rules. Also inserts
 * {@link RateLimitingFilter} into the chain and exposes the
 * {@link PasswordEncoder} used everywhere passwords are hashed or checked.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthSuccessHandler customAuthSuccessHandler;
    private final CustomAuthFailureHandler customAuthFailureHandler;
    private final RateLimitingFilter rateLimitingFilter;

    /**
     * Defines the security filter chain: public routes (sign-in/up, static
     * assets, actuator health, API docs) vs. everything else requiring
     * authentication; form login wired to {@link #customAuthSuccessHandler}/
     * {@link #customAuthFailureHandler}; session logout at {@code /sign-out};
     * and rate limiting inserted right after Spring's own login filter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/sign-in", "/sign-up", "/sign-in-failure",
                                "/css/**", "/images/**", "/js/**",
                                "/actuator/health", "/actuator/health/**"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**",
                                "/scalar", "/scalar/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/sign-in")
                        .loginProcessingUrl("/sign-in")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customAuthSuccessHandler)
                        .failureHandler(customAuthFailureHandler)
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/sign-out")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/sign-in")
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl("/sign-in?expired")
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; "
                                        + "script-src 'self' https://cdn.jsdelivr.net; "
                                        + "style-src 'self' https://cdn.jsdelivr.net https://use.fontawesome.com https://cdnjs.cloudflare.com 'unsafe-inline'; "
                                        + "font-src 'self' https://cdn.jsdelivr.net https://use.fontawesome.com https://cdnjs.cloudflare.com data:; "
                                        + "img-src 'self' data:; "
                                        + "connect-src 'self'; "
                                        + "object-src 'none'; "
                                        + "base-uri 'self'; "
                                        + "form-action 'self'; "
                                        + "frame-ancestors 'none'"
                        ))
                )
                .addFilterAfter(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Tracks each principal's active sessions so
     * {@link org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy}
     * can enforce {@code maximumSessions(1)}. Must be a singleton bean (not
     * created inline) so the same instance is shared between the security
     * filter chain and HttpSessionEventPublisher, which needs it to
     * remove sessions from the registry when they're invalidated or expire.
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /** Exposes Spring Security's default {@link AuthenticationManager}, used by {@link com.weatherviewer.service.LoginService}. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** BCrypt password encoder (strength 12) used to hash and verify all stored passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

}
