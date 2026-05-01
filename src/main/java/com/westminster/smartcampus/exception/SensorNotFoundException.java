package com.westminster.smartcampus.exception;

public class SensorNotFoundException extends RuntimeException {
    public SensorNotFoundException(String message) {
        super(message); // 404 – sensor ID does not exist
    }
}