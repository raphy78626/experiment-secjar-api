package com.secjar.secjarapi.exceptions;

public class BadEmailException extends RuntimeException{
    public BadEmailException(String message) {
        super(message);
    }
}
