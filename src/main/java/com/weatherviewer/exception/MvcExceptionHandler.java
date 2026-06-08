package com.weatherviewer.exception;

import com.weatherviewer.controller.*;
import com.weatherviewer.exception.notfound.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = {
        HomeController.class,
        SearchController.class,
        ForecastController.class,
        ProfileController.class,
        AuthController.class
})
@Slf4j
public class MvcExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException ex, RedirectAttributes ra) {
        log.warn("NotFoundException in MVC: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex, RedirectAttributes ra) {
        log.warn("AccessDeniedException in MVC: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", "You don't have permission to do that");
        return "redirect:/";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParam(MissingServletRequestParameterException ex, RedirectAttributes ra) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        ra.addFlashAttribute("errorMessage", "Invalid request");
        return "redirect:/";
    }

    @ExceptionHandler(ExternalHttpCallException.class)
    public String handleExternalHttpCallException(ExternalHttpCallException ex, RedirectAttributes ra) {
        log.error("External API error in MVC: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", "Weather service unavailable, please try again later");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, RedirectAttributes ra) {
        log.error("Unexpected error in MVC: {}", ex.getMessage(), ex);
        ra.addFlashAttribute("errorMessage", "Something went wrong, please try again");
        return "error";
    }

}
