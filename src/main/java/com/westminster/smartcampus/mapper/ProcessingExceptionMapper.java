package com.westminster.smartcampus.mapper;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.westminster.smartcampus.dto.ErrorResponse;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider 
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {

    private static final Logger LOGGER = Logger.getLogger(ProcessingExceptionMapper.class.getName());

    @Override
    public Response toResponse(ProcessingException exception) {

        // 400 if body is missing 
        if (exception.getCause() instanceof JsonMappingException) {

            LOGGER.log(Level.WARNING,
                "Request body is missing or could not be deserialised: {0}",
                exception.getCause().getMessage());

            ErrorResponse error = new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Bad Request",
                "Request body is missing or malformed. " +
                "Please provide a valid JSON payload."
            );

            return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build(); // 400 Bad Request with JSON error body
        }

        LOGGER.log(Level.SEVERE,
            "Unexpected ProcessingException intercepted by ProcessingExceptionMapper",
            exception);

        ErrorResponse error = new ErrorResponse(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error",
            "An unexpected error occurred. Please contact the system administrator."
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build(); 
    }
}

