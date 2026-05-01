package com.westminster.smartcampus.service;

import com.westminster.smartcampus.datastore.SensorReadingStore;
import com.westminster.smartcampus.datastore.SensorStore;
import com.westminster.smartcampus.exception.SensorNotFoundException;
import com.westminster.smartcampus.exception.SensorUnavailableException;
import com.westminster.smartcampus.exception.ValidationException;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;
import com.westminster.smartcampus.util.IdGenerator;
import com.westminster.smartcampus.util.StatusConstants;
import com.westminster.smartcampus.util.TimeUtil;

import java.util.List;

public class SensorReadingService {

    private static final SensorReadingService INSTANCE = new SensorReadingService(); // single shared instance
    private final SensorReadingStore readingStore = SensorReadingStore.getInstance();
    private final SensorStore sensorStore = SensorStore.getInstance();

    private SensorReadingService() {}

    public static SensorReadingService getInstance() {
        return INSTANCE;
    }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        Sensor sensor = sensorStore.findById(sensorId);
        if (sensor == null) {
            throw new SensorNotFoundException("Sensor not found with id: " + sensorId); // 404
        }
        return readingStore.findBySensorId(sensorId); // 200 – empty list if no readings yet
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        Sensor sensor = sensorStore.findById(sensorId);
        if (sensor == null) {
            throw new SensorNotFoundException("Sensor not found with id: " + sensorId); // 404
        }

        // State constraint: only ACTIVE sensors accept new readings
        if (!StatusConstants.ACTIVE.equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException( // 403 – MAINTENANCE or OFFLINE
                "Sensor '" + sensorId + "' is currently in '" + sensor.getStatus() +
                "' status and cannot accept new readings. " +
                "Only sensors with ACTIVE status are able to record readings."
            );
        }

        validateReading(reading); // 400 if payload is null

        // Assign metadata
        reading.setId(IdGenerator.generateId()); // UUID assigned here
        reading.setSensorId(sensorId);
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(TimeUtil.nowMillis()); // auto-set if client did not provide timestamp
        }

        readingStore.save(reading);

        sensor.setCurrentValue(reading.getValue()); // keeps sensor currentValue in sync with latest reading

        return reading; // 201 on success
    }

    private void validateReading(SensorReading reading) {
        if (reading == null) {
            throw new ValidationException("SensorReading payload must not be null."); // 400
        }
    }
}

