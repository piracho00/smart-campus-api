package com.westminster.smartcampus.util;

public class TimeUtil {

    private TimeUtil() {} 

    public static long nowMillis() {
        return System.currentTimeMillis(); // used to auto-stamp readings when client omits timestamp
    }
}
