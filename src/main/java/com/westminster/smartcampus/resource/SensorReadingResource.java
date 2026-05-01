package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.model.SensorReading;
import com.westminster.smartcampus.service.SensorReadingService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final SensorReadingService readingService = SensorReadingService.getInstance();
    private final UriInfo uriInfo; // passed in from SensorResource locator — avoids @Context injection failure

    public SensorReadingResource(String sensorId, UriInfo uriInfo) {
        this.sensorId = sensorId;
        this.uriInfo = uriInfo;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> readings = readingService.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        SensorReading created = readingService.addReading(sensorId, reading);
        URI location = uriInfo.getAbsolutePathBuilder()
            .path(created.getId())
            .build();
        return Response.created(location).entity(created).build();
    }
}