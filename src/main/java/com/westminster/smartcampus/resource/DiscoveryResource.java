package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.dto.ApiInfoResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/") // maps to GET /api/v1
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @Context
    private UriInfo uriInfo; 

    @GET
    public Response discover() {
        String baseUri = uriInfo.getBaseUri().toString();
        if (baseUri.endsWith("/")) {
            baseUri = baseUri.substring(0, baseUri.length() - 1);
        }

        Map<String, String> links = new LinkedHashMap<>(); // ordered map preserves link insertion order in JSON
        links.put("self",     baseUri + "/");
        links.put("rooms",    baseUri + "/rooms");    // 200 entry point for Room collection
        links.put("sensors",  baseUri + "/sensors");  // 200 entry point for Sensor collection

        ApiInfoResponse info = new ApiInfoResponse();
        info.setName("Smart Campus Sensor & Room Management API");
        info.setVersion("1.0.0");
        info.setDescription(
            "A RESTful API for managing campus rooms and their associated IoT sensors " +
            "as part of the University of Westminster Smart Campus initiative."
        );
        info.setContact("admin@smartcampus.westminster.ac.uk");
        info.setTimestamp(System.currentTimeMillis()); 
        info.setLinks(links);

        return Response.ok(info).build(); // 200 OK with HATEOAS metadata
    }
}

