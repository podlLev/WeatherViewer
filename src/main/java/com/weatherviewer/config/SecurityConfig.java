package com.weatherviewer.config;

import com.weatherviewer.ratelimit.RateLimitingFilter;
import com.weatherviewer.security.CustomAuthFailureHandler;
import com.weatherviewer.security.CustomAuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.sql.DataSource;

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
    private final DataSource dataSource;
    private final UserDetailsService userDetailsService;

    /**
     * Secret used to sign remember-me tokens. Must be a long, random,
     * environment-specific value (e.g. {@code openssl rand -hex 32}) —
     * never left at a default in production, since anyone who knows it
     * could forge a valid remember-me cookie.
     */
    @Value("${security.remember-me.key}")
    private String rememberMeKey;

    /** How long a remember-me cookie stays valid before the user must sign in again. Defaults to 14 days. */
    @Value("${security.remember-me.token-validity-seconds:1209600}")
    private int rememberMeTokenValiditySeconds;

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
                                "/verify-email", "/resend-verification",
                                "/forgot-password", "/reset-password",
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
                        .deleteCookies("JSESSIONID", "remember-me")
                        .addLogoutHandler(rememberMeServices())
                        .logoutSuccessUrl("/sign-in")
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl("/sign-in?expired")
                )
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeServices(rememberMeServices())
                        .key(rememberMeKey)
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self' https://cdn.jsdelivr.net; " +
                                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://use.fontawesome.com https://cdnjs.cloudflare.com https://fonts.googleapis.com; " +
                                                "font-src 'self' https://cdn.jsdelivr.net https://fonts.gstatic.com https://use.fontawesome.com https://cdnjs.cloudflare.com; " +
                                                "connect-src 'self' https://cdn.jsdelivr.net; " +
                                                "img-src 'self' data: https:;"
                                )
                        )
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

    /**
     * Backs remember-me tokens with the {@code persistent_logins} table
     * (schema defined in the {@code 06-add-persistent-logins.xml} Liquibase
     * changeset) instead of Spring Security's simpler hash-based cookie.
     * This lets a stolen/leaked cookie be invalidated server-side and
     * rotates the token on every use, so a captured cookie is only valid
     * for a single re-use before it's detected and the whole series is
     * revoked.
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    /**
     * Issues and validates the {@code remember-me} cookie. Token validity
     * is configurable via {@code security.remember-me.token-validity-seconds}
     * (default 14 days); the signing key comes from
     * {@code security.remember-me.key} and must be set per-environment.
     */
    @Bean
    public PersistentTokenBasedRememberMeServices rememberMeServices() {
        PersistentTokenBasedRememberMeServices services =
                new PersistentTokenBasedRememberMeServices(rememberMeKey, userDetailsService, persistentTokenRepository());
        services.setTokenValiditySeconds(rememberMeTokenValiditySeconds);
        services.setParameter("remember-me");
        services.setCookieName("remember-me");
        services.setUseSecureCookie(true);
        return services;
    }

}
