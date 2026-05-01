package com.westminster.smartcampus.util;

public class StatusConstants {

    private StatusConstants() {} 

    public static final String ACTIVE = "ACTIVE";           // sensor is operational and accepts readings
    public static final String MAINTENANCE = "MAINTENANCE"; // sensor is under maintenance; readings blocked (403)
    public static final String OFFLINE = "OFFLINE";         // sensor is disconnected; readings blocked (403)
}
