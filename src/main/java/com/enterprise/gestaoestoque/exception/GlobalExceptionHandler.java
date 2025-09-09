package com.enterprise.gestaoestoque.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exc) {
        var error = new ErrorResponse();

        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(exc.getMessage());
        error.setTimestamp(LocalDateTime.now().toString());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException exc) {
        Map<String, Object> error = new LinkedHashMap<>();

        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("message", exc.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());

        if (exc.getCause() instanceof Map<?,?> errors) {
            error.put("errors", errors);
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException exc) {
        Map<String, Object> error = new LinkedHashMap<>();

        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("message", exc.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());

        if (exc.getCause() instanceof Map<?,?> errors) {
            error.put("errors", errors);
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
