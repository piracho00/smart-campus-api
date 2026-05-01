package com.westminster.smartcampus.datastore;

import com.westminster.smartcampus.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomStore {

    private static final RoomStore INSTANCE = new RoomStore(); 
    private final Map<String, Room> rooms = new ConcurrentHashMap<>(); 

    private RoomStore() {}

    public static RoomStore getInstance() {
        return INSTANCE;
    }

    public void save(Room room) {
        rooms.put(room.getId(), room); 
    }

    public Room findById(String id) {
        return rooms.get(id); 
    }

    public List<Room> findAll() {
        return new ArrayList<>(rooms.values()); 
    }

    public boolean existsById(String id) {
        return rooms.containsKey(id);
    }

    public void deleteById(String id) {
        rooms.remove(id); 
    }
}
