package com.westminster.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message); // 422 – referenced resource not found in a valid payload
    }
}