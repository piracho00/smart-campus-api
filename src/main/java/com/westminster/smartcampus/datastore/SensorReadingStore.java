package com.westminster.smartcampus.datastore;

import com.westminster.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorReadingStore {

    private static final SensorReadingStore INSTANCE = new SensorReadingStore(); 
    private final Map<String, List<SensorReading>> readingsBySensor = new ConcurrentHashMap<>(); 

    private SensorReadingStore() {}

    public static SensorReadingStore getInstance() {
        return INSTANCE;
    }

    public void save(SensorReading reading) {
        readingsBySensor
            .computeIfAbsent(reading.getSensorId(), k -> new ArrayList<>()) 
            .add(reading);
    }

    public List<SensorReading> findBySensorId(String sensorId) {
        return new ArrayList<>(readingsBySensor.getOrDefault(sensorId, new ArrayList<>())); 
    }

    public SensorReading findByIdAndSensorId(String id, String sensorId) {
        List<SensorReading> readings = readingsBySensor.getOrDefault(sensorId, new ArrayList<>());
        return readings.stream()
            .filter(r -> r.getId().equals(id))
            .findFirst()
            .orElse(null); 
    }
}