package ca.gbc.roomservice.controller;

import ca.gbc.roomservice.model.Room;
import ca.gbc.roomservice.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    // Get all available rooms
    @GetMapping("/available")
    public ResponseEntity<List<Room>> getAllAvailableRooms() {
        List<Room> availableRooms = roomRepository.findAllAvailableRooms();
        return ResponseEntity.ok(availableRooms);
    }

    // Get a room by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new room
    @PostMapping
    public ResponseEntity<String> createRoom(@RequestBody Room room) {
        int rowsAffected = roomRepository.save(room);
        if (rowsAffected > 0) {
            return ResponseEntity.ok("Room created successfully.");
        } else {
            return ResponseEntity.status(500).body("Failed to create room.");
        }
    }

    // Update an existing room
    @PutMapping("/{id}")
    public ResponseEntity<String> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        roomDetails.setId(id);
        int rowsAffected = roomRepository.update(roomDetails);
        if (rowsAffected > 0) {
            return ResponseEntity.ok("Room updated successfully.");
        } else {
            return ResponseEntity.status(404).body("Room not found.");
        }
    }

    // Delete a room
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        int rowsAffected = roomRepository.delete(id);
        if (rowsAffected > 0) {
            return ResponseEntity.ok("Room deleted successfully.");
        } else {
            return ResponseEntity.status(404).body("Room not found.");
        }
    }
}
