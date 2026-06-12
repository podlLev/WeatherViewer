package com.weatherviewer.exception;

import com.weatherviewer.exception.notfound.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    @InjectMocks
    private ControllerExceptionHandler handler;

    @Test
    void handleNotFoundException_returns404WithMessage() {
        NotFoundException ex = new NotFoundException("City not found");

        ResponseEntity<String> response = handler.handleNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("City not found");
    }

    @Test
    void handleNotFoundException_nullMessage_returns404WithNull() {
        NotFoundException ex = new NotFoundException(null);

        ResponseEntity<String> response = handler.handleNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void handleAccessDeniedException_returns403WithMessage() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<String> response = handler.handleAccessDeniedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Access is denied");
    }

    @Test
    void handleValidationException_returnsFieldErrors() {
        record TestDto(String email, String username) {}

        TestDto target = new TestDto(null, null);
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "target");

        bindingResult.rejectValue("email", "NotBlank", "must not be blank");
        bindingResult.rejectValue("username", "Size", "size must be between 3 and 50");

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<List<FieldError>> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody())
                .extracting(FieldError::field)
                .containsExactlyInAnyOrder("email", "username");
        assertThat(response.getBody())
                .extracting(FieldError::message)
                .containsExactlyInAnyOrder("must not be blank", "size must be between 3 and 50");
    }

    @Test
    void handleValidationException_noErrors_returnsEmptyList() {
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<List<FieldError>> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void handleMethodValidationException_returnsParameterErrors() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);

        var resolvableError = mock(org.springframework.context.MessageSourceResolvable.class);
        when(resolvableError.getDefaultMessage()).thenReturn("must be positive");

        var paramResult = mock(ParameterValidationResult.class);
        MethodParameter mp = mock(MethodParameter.class);
        when(mp.getParameterName()).thenReturn("page");
        when(paramResult.getMethodParameter()).thenReturn(mp);
        when(paramResult.getResolvableErrors()).thenReturn(List.of(resolvableError));

        when(ex.getParameterValidationResults()).thenReturn(List.of(paramResult));

        ResponseEntity<Map<String, String>> response = handler.handleMethodValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("page", "must be positive");
    }

    @Test
    void handleMethodValidationException_nullMessage_fallsBackToDefaultText() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);

        var resolvableError = mock(org.springframework.context.MessageSourceResolvable.class);
        when(resolvableError.getDefaultMessage()).thenReturn(null);

        var paramResult = mock(ParameterValidationResult.class);
        MethodParameter mp = mock(MethodParameter.class);
        when(mp.getParameterName()).thenReturn("size");
        when(paramResult.getMethodParameter()).thenReturn(mp);
        when(paramResult.getResolvableErrors()).thenReturn(List.of(resolvableError));

        when(ex.getParameterValidationResults()).thenReturn(List.of(paramResult));

        ResponseEntity<Map<String, String>> response = handler.handleMethodValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("size", "Invalid value");
    }

    @Test
    void handleMethodValidationException_duplicateParams_keepsFirstEntry() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);

        var error1 = mock(org.springframework.context.MessageSourceResolvable.class);
        when(error1.getDefaultMessage()).thenReturn("first error");
        var error2 = mock(org.springframework.context.MessageSourceResolvable.class);
        when(error2.getDefaultMessage()).thenReturn("second error");

        var paramResult1 = mock(ParameterValidationResult.class);
        var paramResult2 = mock(ParameterValidationResult.class);

        MethodParameter mp = mock(MethodParameter.class);
        when(mp.getParameterName()).thenReturn("id");

        when(paramResult1.getMethodParameter()).thenReturn(mp);
        when(paramResult1.getResolvableErrors()).thenReturn(List.of(error1));
        when(paramResult2.getMethodParameter()).thenReturn(mp);
        when(paramResult2.getResolvableErrors()).thenReturn(List.of(error2));

        when(ex.getParameterValidationResults()).thenReturn(List.of(paramResult1, paramResult2));

        ResponseEntity<Map<String, String>> response = handler.handleMethodValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("id", "first error");
    }

    @Test
    void handleExternalHttpCallException_returns503WithGenericMessage() {
        ExternalHttpCallException ex = new ExternalHttpCallException("Upstream timeout");

        ResponseEntity<String> response = handler.handleExternalHttpCallException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo("Weather service unavailable");
    }

    @Test
    void handleInternalServerError_forSQLException_returns500() {
        SQLException ex = new SQLException("Connection refused");

        ResponseEntity<String> response = handler.handleInternalServerError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Internal server error");
    }

    @Test
    void handleInternalServerError_forIOException_returns500() {
        IOException ex = new IOException("Disk full");

        ResponseEntity<String> response = handler.handleInternalServerError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Internal server error");
    }

    @Test
    void handleUnexpectedException_returns500WithGenericMessage() {
        RuntimeException ex = new RuntimeException("Something completely unexpected");

        ResponseEntity<String> response = handler.handleUnexpectedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Something went wrong");
    }

    @Test
    void handleUnexpectedException_nullMessage_stillReturns500() {
        RuntimeException ex = new RuntimeException((String) null);

        ResponseEntity<String> response = handler.handleUnexpectedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Something went wrong");
    }

}
