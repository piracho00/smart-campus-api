package com.westminster.smartcampus.service;

import com.westminster.smartcampus.datastore.RoomStore;
import com.westminster.smartcampus.datastore.SensorStore;
import com.westminster.smartcampus.exception.LinkedResourceNotFoundException;
import com.westminster.smartcampus.exception.SensorNotFoundException;
import com.westminster.smartcampus.exception.ValidationException;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.util.IdGenerator;
import com.westminster.smartcampus.util.StatusConstants;

import java.util.List;
import java.util.stream.Collectors;

public class SensorService {

    private static final SensorService INSTANCE = new SensorService(); 
    private final SensorStore sensorStore = SensorStore.getInstance();
    private final RoomStore roomStore = RoomStore.getInstance();

    private SensorService() {}

    public static SensorService getInstance() {
        return INSTANCE;
    }

    public List<Sensor> getAllSensors(String typeFilter) {
        List<Sensor> all = sensorStore.findAll();
        if (typeFilter == null || typeFilter.trim().isEmpty()) {
            return all; 
        }
        return all.stream()
            .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(typeFilter.trim())) // case-insensitive type match
            .collect(Collectors.toList()); // empty list if no sensors match the type
    }

    public Sensor getSensorById(String id) {
        Sensor sensor = sensorStore.findById(id);
        if (sensor == null) {
            throw new SensorNotFoundException("Sensor not found with id: " + id); // 404
        }
        return sensor;
    }

    public Sensor createSensor(Sensor sensor) {
        validateSensor(sensor); // 400 if type or roomId is missing, or status is invalid

        // Verify the referenced room exists
        Room room = roomStore.findById(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException( // 422 – roomId not found
                "Cannot create sensor: the referenced roomId '" + sensor.getRoomId() +
                "' does not exist in the system. Ensure the room is created before registering sensors."
            );
        }

        // Assign ID and defaults
        sensor.setId(IdGenerator.generateId()); // UUID assigned here
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus(StatusConstants.ACTIVE); // default status when not provided
        }

        sensorStore.save(sensor);

        // Register sensor in the parent room
        room.getSensorIds().add(sensor.getId()); // keeps room sensorIds list in sync

        return sensor; // 201 on success
    }

    private void validateSensor(Sensor sensor) {
        if (sensor == null) {
            throw new ValidationException("Sensor payload must not be null."); // 400
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            throw new ValidationException("Sensor 'type' is required and must not be empty."); // 400
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            throw new ValidationException("Sensor 'roomId' is required and must not be empty."); // 400
        }
        // Validate status if provided
        if (sensor.getStatus() != null && !sensor.getStatus().trim().isEmpty()) {
            String status = sensor.getStatus().toUpperCase();
            if (!status.equals(StatusConstants.ACTIVE) &&
                !status.equals(StatusConstants.MAINTENANCE) &&
                !status.equals(StatusConstants.OFFLINE)) {
                throw new ValidationException(
                    "Sensor 'status' must be one of: ACTIVE, MAINTENANCE, OFFLINE." // 400
                );
            }
        }
    }
}

