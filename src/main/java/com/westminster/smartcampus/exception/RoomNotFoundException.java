package com.westminster.smartcampus.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message); // 404 – room ID does not exist
    }
}