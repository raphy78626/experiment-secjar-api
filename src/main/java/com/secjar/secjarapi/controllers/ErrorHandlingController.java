package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.exceptions.InternalException;
import com.secjar.secjarapi.exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandlingController {

    @ExceptionHandler({ResourceNotFoundException.class})
    protected ResponseEntity<Object> handleResourceNotFoundException(Exception ex, WebRequest request) {
        Map<String, Object> errorMessage = createExceptionResponseBody(ex.getMessage(), 404);

        return ResponseEntity.status(404).body(errorMessage);
    }

    @ExceptionHandler({InternalException.class})
    protected ResponseEntity<Object> handleInternalException(Exception ex, WebRequest request) {
        Map<String, Object> errorMessage = createExceptionResponseBody(ex.getMessage(), 500);
        return ResponseEntity.status(500).body(errorMessage);
    }

    @ExceptionHandler({BadCredentialsException.class})
    protected ResponseEntity<Object> handleBadCredentialsException(Exception ex, WebRequest request) {
        Map<String, Object> errorMessage = createExceptionResponseBody("Wrong username, password or 2FA code", 400);
        return ResponseEntity.status(400).body(errorMessage);
    }

    private static Map<String, Object> createExceptionResponseBody(String message, int statusCode) {
        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("error", message);
        errorMessage.put("status", statusCode);
        errorMessage.put("time_stamp", LocalDateTime.now());
        return errorMessage;
    }
}
