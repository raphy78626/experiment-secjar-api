package com.secjar.secjarapi.controllers;

import com.secjar.secjarapi.exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
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
        Map<String, Object> errorMessage = createExceptionResponseBody(ex, 404);

        return ResponseEntity.status(404).body(errorMessage);
    }

    private static Map<String, Object> createExceptionResponseBody(Exception originalException, int statusCode) {
        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("error", originalException.getMessage());
        errorMessage.put("status", statusCode);
        errorMessage.put("time_stamp", LocalDateTime.now());
        return errorMessage;
    }
}
