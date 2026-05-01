package com.westminster.smartcampus.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1") // base path for all API endpoints
public class ApplicationConfig extends Application {
}

