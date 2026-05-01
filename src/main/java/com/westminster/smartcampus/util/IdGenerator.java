package com.westminster.smartcampus.util;

import java.util.UUID;

public class IdGenerator {

    private IdGenerator() {} 

    public static String generateId() {
        return UUID.randomUUID().toString(); // random  UUID
    }
}
