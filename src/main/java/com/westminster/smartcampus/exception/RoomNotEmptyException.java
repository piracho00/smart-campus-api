package com.westminster.smartcampus.exception;

public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message); // 409 – cannot delete room while sensors are assigned
    }
}
