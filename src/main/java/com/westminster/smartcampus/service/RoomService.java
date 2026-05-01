package com.westminster.smartcampus.service;

import com.westminster.smartcampus.datastore.RoomStore;
import com.westminster.smartcampus.datastore.SensorStore;
import com.westminster.smartcampus.exception.RoomNotFoundException;
import com.westminster.smartcampus.exception.RoomNotEmptyException;
import com.westminster.smartcampus.exception.ValidationException;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.util.IdGenerator;

import java.util.List;

public class RoomService {

    private static final RoomService INSTANCE = new RoomService(); // single shared instance
    private final RoomStore roomStore = RoomStore.getInstance();
    private final SensorStore sensorStore = SensorStore.getInstance();

    private RoomService() {}

    public static RoomService getInstance() {
        return INSTANCE;
    }

    public List<Room> getAllRooms() {
        return roomStore.findAll(); // 200 – empty list if no rooms exist
    }

    public Room getRoomById(String id) {
        Room room = roomStore.findById(id);
        if (room == null) {
            throw new RoomNotFoundException("Room not found with id: " + id); // 404
        }
        return room;
    }

    public Room createRoom(Room room) {
        validateRoom(room); // 400 if name is blank or capacity is not positive
        room.setId(IdGenerator.generateId()); // UUID assigned here
        roomStore.save(room);
        return room;
    }

    public void deleteRoom(String id) {
        Room room = roomStore.findById(id);
        if (room == null) {
            throw new RoomNotFoundException("Room not found with id: " + id); // 404
        }
        // Safety constraint: cannot delete a room that has sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException( // 409 – deletion blocked
                "Cannot delete room '" + id + "'. It still has " +
                room.getSensorIds().size() + " sensor(s) assigned. " +
                "Remove all sensors before decommissioning the room."
            );
        }
        roomStore.deleteById(id); // 204 on success
    }

    private void validateRoom(Room room) {
        if (room == null) {
            throw new ValidationException("Room payload must not be null."); // 400
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new ValidationException("Room 'name' is required and must not be empty."); // 400
        }
        if (room.getCapacity() <= 0) {
            throw new ValidationException("Room 'capacity' must be a positive integer."); // 400
        }
    }
}
