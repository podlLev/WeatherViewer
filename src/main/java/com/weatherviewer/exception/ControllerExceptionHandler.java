
package com.weatherviewer.exception;

import com.weatherviewer.exception.notfound.NotFoundException;
import com.weatherviewer.rest.LocationController;
import com.weatherviewer.rest.UserController;
import com.weatherviewer.rest.WeatherController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Global exception handler for the JSON REST API.
 * <p>
 * Scoped via {@code assignableTypes} to only the three
 * {@code @RestController}s under {@code /api/v1/} ({@link LocationController},
 * {@link UserController}, {@link WeatherController}), so the Thymeleaf web
 * app's exceptions continue to be handled separately by
 * {@link MvcExceptionHandler}. Each handler here converts a specific
 * exception type into an appropriate HTTP status and response body; see
 * the API reference documentation for the full list of status codes an
 * API client should expect.
 */
@RestControllerAdvice(assignableTypes = {
        LocationController.class,
        UserController.class,
        WeatherController.class
})
@Slf4j
public class ControllerExceptionHandler {

    /** Maps any "resource not found" exception to {@code 404 Not Found} with the exception's message as the body. */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
        log.warn("NotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /** Maps Spring Security's access-denied exception to {@code 403 Forbidden} (authenticated, but missing the required authority). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("AccessDeniedException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /** Maps Bean Validation failures on a {@code @RequestBody} to {@code 422 Unprocessable Entity} with a list of per-field errors. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<FieldError>> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(result -> new FieldError(result.getField(), result.getDefaultMessage()))
                .toList();
        log.info("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
    }

    /** Maps a manually-triggered {@link LocationValidationException} to {@code 422 Unprocessable Entity} with its captured field errors. */
    @ExceptionHandler(LocationValidationException.class)
    public ResponseEntity<List<FieldError>> handleLocationValidationException(LocationValidationException ex) {
        log.info("Location validation errors: {}", ex.getErrors());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getErrors());
    }

    /** Maps Bean Validation failures on method/query parameters (not a request body) to {@code 400 Bad Request}, keyed by parameter name. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> handleMethodValidationException(HandlerMethodValidationException ex) {
        Map<String, String> errors = ex.getParameterValidationResults().stream()
                .collect(Collectors.toMap(
                        result -> result.getMethodParameter().getParameterName(),
                        result -> {
                            String message = result.getResolvableErrors().get(0).getDefaultMessage();
                            return Objects.requireNonNullElse(message, "Invalid value");
                        },
                        (existing, replacement) -> existing
                ));

        log.info("Parameter validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /** Maps a failed call to the upstream OpenWeatherMap API to {@code 503 Service Unavailable} with a generic, client-safe message. */
    @ExceptionHandler(ExternalHttpCallException.class)
    public ResponseEntity<String> handleExternalHttpCallException(ExternalHttpCallException ex) {
        log.error("External API error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Weather service unavailable");
    }

    /** Maps low-level database/IO failures to {@code 500 Internal Server Error}, logging the full stack trace but returning a generic message. */
    @ExceptionHandler({SQLException.class, IOException.class})
    public ResponseEntity<String> handleInternalServerError(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }

    /** Catch-all for any exception not handled above; logs the full stack trace and returns a generic {@code 500} response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
    }

}
