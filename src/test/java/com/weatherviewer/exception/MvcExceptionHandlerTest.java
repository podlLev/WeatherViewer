package com.weatherviewer.exception;

import com.weatherviewer.exception.notfound.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MvcExceptionHandlerTest {

    @InjectMocks
    private MvcExceptionHandler handler;

    @Test
    void handleNotFoundException_redirectsToHomeWithFlashAttribute() {
        NotFoundException ex = new NotFoundException("User session expired");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = handler.handleNotFoundException(ex, ra);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(ra.getFlashAttributes().get("errorMessage"))
                .hasToString("User session expired");
    }

    @Test
    void handleAccessDeniedException_redirectsToHomeWithCustomMessage() {
        AccessDeniedException ex = new AccessDeniedException("No roles allowed");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = handler.handleAccessDeniedException(ex, ra);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(ra.getFlashAttributes().get("errorMessage"))
                .hasToString("You don't have permission to do that");
    }

    @Test
    void handleMissingParam_redirectsToHomeWithGenericMessage() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("cityName", "String");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = handler.handleMissingParam(ex, ra);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(ra.getFlashAttributes().get("errorMessage"))
                .hasToString("Invalid request");
    }

    @Test
    void handleMvcMethodValidationException_collectsErrorsAndRedirects() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        MessageSourceResolvable error1 = mock(MessageSourceResolvable.class);
        when(error1.getDefaultMessage()).thenReturn("Invalid coordinates");
        MessageSourceResolvable error2 = mock(MessageSourceResolvable.class);
        when(error2.getDefaultMessage()).thenReturn("City name is too long");

        ParameterValidationResult paramResult1 = mock(ParameterValidationResult.class);
        ParameterValidationResult paramResult2 = mock(ParameterValidationResult.class);

        when(paramResult1.getResolvableErrors()).thenReturn(List.of(error1));
        when(paramResult2.getResolvableErrors()).thenReturn(List.of(error2));
        when(ex.getParameterValidationResults()).thenReturn(List.of(paramResult1, paramResult2));

        String view = handler.handleMvcMethodValidationException(ex, ra);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(ra.getFlashAttributes().get("errorMessage"))
                .hasToString("Invalid coordinates, City name is too long");
    }

    @Test
    void handleMvcMethodValidationException_emptyErrors_fallsBackToDefaultText() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        ParameterValidationResult paramResult = mock(ParameterValidationResult.class);
        when(paramResult.getResolvableErrors()).thenReturn(Collections.emptyList());
        when(ex.getParameterValidationResults()).thenReturn(List.of(paramResult));

        String view = handler.handleMvcMethodValidationException(ex, ra);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(ra.getFlashAttributes().get("errorMessage"))
                .hasToString("Invalid input data provided");
    }

    @Test
    void handleExternalHttpCallException_returnsErrorViewWithFlashAttribute() {
        ExternalHttpCallException ex = new ExternalHttpCallException("OpenWeather API rate limit reached");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = handler.handleExternalHttpCallException(ex, ra);

        assertThat(view).isEqualTo("error");
        assertThat(ra.getFlashAttributes().get("errorMessage"))
                .hasToString("Weather service unavailable, please try again later");
    }

    @Test
    void handleException_returnsErrorViewWithFallbackMessage() {
        RuntimeException ex = new RuntimeException("Uncaught template parsing exception");
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String view = handler.handleException(ex, ra);

        assertThat(view).isEqualTo("error");
        assertThat(ra.getFlashAttributes().get("errorMessage"))
                .hasToString("Something went wrong, please try again");
    }

}
