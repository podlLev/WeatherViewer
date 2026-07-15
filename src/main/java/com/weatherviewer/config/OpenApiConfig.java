package com.weatherviewer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Central OpenAPI/Swagger configuration.
 * <p>
 * Defines the API metadata shown in Swagger UI, the available servers,
 * and the authentication scheme used to authorize requests from the
 * "Authorize" button (session cookie issued by the form-login flow at
 * {@code POST /sign-in}).
 */
@Configuration
public class OpenApiConfig {

    private static final String SESSION_COOKIE_SCHEME = "sessionCookieAuth";

    @Bean
    public OpenAPI weatherViewerOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("/").description("Current environment"),
                        new Server().url("http://localhost:8080").description("Local development")
                ))
                .tags(List.of(
                        new Tag().name("Weather")
                                .description("Current conditions, hourly/daily forecasts and city geocoding, backed by OpenWeatherMap"),
                        new Tag().name("Locations - My")
                                .description("Endpoints operating on the authenticated caller's own saved locations"),
                        new Tag().name("Locations - Admin")
                                .description("Admin-only endpoints for managing any user's locations. Require the users:write authority"),
                        new Tag().name("Users")
                                .description("User account management. All endpoints require the users:write authority")
                ))
                .components(new Components()
                        .addSecuritySchemes(SESSION_COOKIE_SCHEME, sessionCookieScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SESSION_COOKIE_SCHEME));
    }

    private Info apiInfo() {
        return new Info()
                .title("WeatherViewer API")
                .description("""
                        REST API for WeatherViewer: manage user accounts, save and organize favorite \
                        locations, and fetch current, hourly and daily weather forecasts.
 
                        ### Authentication
                        The API uses the same session-cookie authentication as the web application. \
                        Log in via `POST /sign-in` (form fields `email` and `password`) to obtain a \
                        `JSESSIONID` cookie, then include that cookie on subsequent requests. State-changing \
                        requests also require a valid CSRF token issued as an `XSRF-TOKEN` cookie.
 
                        **Getting started with curl:**
                        ```
                        curl -c cookies.txt -X POST http://localhost:8080/sign-in \\
                          -d "email=jane.doe@example.com&password=yourPassword"
 
                        curl -b cookies.txt http://localhost:8080/api/v1/locations/my
                        ```
 
                        ### Errors
                        Errors are returned as either a plain-text message, a list of field errors, or a \
                        map of parameter errors, depending on the failure - see the response schemas on \
                        each endpoint for details.
                        """)
                .version("v1")
                .contact(new Contact().name("WeatherViewer"))
                .license(new License().name("Internal use"));
    }

    private SecurityScheme sessionCookieScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID")
                .description("Session cookie obtained by logging in via POST /sign-in");
    }

}
