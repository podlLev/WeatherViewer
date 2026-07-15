package com.weatherviewer.exception;

import com.weatherviewer.controller.*;
import com.weatherviewer.exception.notfound.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler for the Thymeleaf web app.
 * <p>
 * Scoped via {@code assignableTypes} to the MVC controllers under
 * {@code controller/} — the counterpart to {@link ControllerExceptionHandler},
 * which handles the same exception types for the JSON REST API. Rather
 * than returning structured error bodies, handlers here flash a
 * human-readable {@code errorMessage} and either redirect back to the
 * dashboard or render the generic {@code error} view.
 */
@ControllerAdvice(assignableTypes = {
        HomeController.class,
        SearchController.class,
        ForecastController.class,
        ProfileController.class,
        AuthController.class
})
@Slf4j
public class MvcExceptionHandler {

    /** Flashes the exception's message and redirects to the dashboard when a requested resource doesn't exist. */
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException ex, RedirectAttributes ra) {
        log.warn("NotFoundException in MVC: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    /** Flashes a generic permission-denied message and redirects to the dashboard when the user lacks the required authority. */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex, RedirectAttributes ra) {
        log.warn("AccessDeniedException in MVC: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", "You don't have permission to do that");
        return "redirect:/";
    }

    /** Flashes a generic "invalid request" message and redirects to the dashboard when a required request parameter is missing. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParam(MissingServletRequestParameterException ex, RedirectAttributes ra) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        ra.addFlashAttribute("errorMessage", "Invalid request");
        return "redirect:/";
    }

    /**
     * Flashes the collected, deduplicated validation messages (falling back
     * to a generic message if none are present) and redirects to the
     * dashboard when Bean Validation fails on an MVC controller's
     * method/query parameters.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public String handleMvcMethodValidationException(HandlerMethodValidationException ex, RedirectAttributes ra) {
        String errorMessage = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(org.springframework.context.MessageSourceResolvable::getDefaultMessage)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));
        if (errorMessage.isEmpty()) {
            errorMessage = "Invalid input data provided";
        }

        log.warn("Validation failures in MVC controller parameters: {}", errorMessage);
        ra.addFlashAttribute("errorMessage", errorMessage);
        return "redirect:/";
    }

    /** Flashes a "service unavailable" message and renders the generic error page when the upstream weather API call fails. */
    @ExceptionHandler(ExternalHttpCallException.class)
    public String handleExternalHttpCallException(ExternalHttpCallException ex, RedirectAttributes ra) {
        log.error("External API error in MVC: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", "Weather service unavailable, please try again later");
        return "error";
    }

    /** Catch-all for any exception not handled above; logs the full stack trace and renders the generic error page with a generic message. */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, RedirectAttributes ra) {
        log.error("Unexpected error in MVC: {}", ex.getMessage(), ex);
        ra.addFlashAttribute("errorMessage", "Something went wrong, please try again");
        return "error";
    }

}
