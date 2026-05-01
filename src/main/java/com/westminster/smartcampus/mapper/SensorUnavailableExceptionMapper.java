package com.westminster.smartcampus.mapper;

import com.westminster.smartcampus.dto.ErrorResponse;
import com.westminster.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider 
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorResponse error = new ErrorResponse(
            Response.Status.FORBIDDEN.getStatusCode(),
            "Forbidden",
            exception.getMessage()
        );
        return Response.status(Response.Status.FORBIDDEN) // 403 – sensor is MAINTENANCE or OFFLINE
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}