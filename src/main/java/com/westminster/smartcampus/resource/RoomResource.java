package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.service.RoomService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final RoomService roomService = RoomService.getInstance();

    @Context
    private UriInfo uriInfo; 

    @GET
    public Response getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return Response.ok(rooms).build(); // 200 OK with room array
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId); // 404 if not found
        return Response.ok(room).build(); // 200 OK with room object
    }

    @POST
    public Response createRoom(Room room) {
        Room created = roomService.createRoom(room); // 400 if validation fails
        URI location = uriInfo.getAbsolutePathBuilder()
            .path(created.getId())
            .build(); 
        return Response.created(location).entity(created).build(); // 201 Created
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        roomService.deleteRoom(roomId); // 404 if not found; 409 if sensors still assigned
        return Response.noContent().build(); // 204 No Content on success
    }
}