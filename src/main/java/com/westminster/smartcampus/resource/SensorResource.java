package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.service.SensorService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorService sensorService = SensorService.getInstance();

    @Context
    private UriInfo uriInfo;

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = sensorService.getAllSensors(type);
        return Response.ok(sensors).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorService.getSensorById(sensorId);
        return Response.ok(sensor).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        Sensor created = sensorService.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder()
            .path(created.getId())
            .build();
        return Response.created(location).entity(created).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId, uriInfo); // FIX: pass injected uriInfo directly — avoids @Context injection failure in sub-resource
    }
}
