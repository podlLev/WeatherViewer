
package com.weatherviewer.exception;

import com.weatherviewer.exception.notfound.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleInvalidTopUpTypeException(Exception ex) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(Exception ex) {
        log.warn("NotFoundException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(result -> new FieldError(result.getField(),
                        result.getDefaultMessage()))
                .toList();

        log.info("Validation errors: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({SQLException.class, IOException.class})
    public ResponseEntity<?> handleInternalServerError(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
