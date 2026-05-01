package com.westminster.smartcampus.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message); // 400 – client sent invalid or incomplete data
    }
}
