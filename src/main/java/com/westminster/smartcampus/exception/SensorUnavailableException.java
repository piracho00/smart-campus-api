package com.westminster.smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message); // 403 – sensor status does not permit new readings
    }
}