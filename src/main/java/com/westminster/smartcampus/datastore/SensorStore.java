package com.westminster.smartcampus.datastore;

import com.westminster.smartcampus.model.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorStore {

    private static final SensorStore INSTANCE = new SensorStore(); 
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>(); 

    private SensorStore() {}

    public static SensorStore getInstance() {
        return INSTANCE;
    }

    public void save(Sensor sensor) {
        sensors.put(sensor.getId(), sensor); 
    }

    public Sensor findById(String id) {
        return sensors.get(id); 
    }

    public List<Sensor> findAll() {
        return new ArrayList<>(sensors.values()); 
    }

    public boolean existsById(String id) {
        return sensors.containsKey(id);
    }

    public void deleteById(String id) {
        sensors.remove(id); 
    }
}
