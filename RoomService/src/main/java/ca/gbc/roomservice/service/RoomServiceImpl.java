package ca.gbc.roomservice.service;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;
import ca.gbc.roomservice.model.Room;
import ca.gbc.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    // Create a new room
    @Override
    public RoomResponse createRoom(RoomRequest roomRequest) {
        Room room = Room.builder()
                .roomName(roomRequest.getRoomName())
                .capacity(roomRequest.getCapacity())
                .features(roomRequest.getFeatures())
                .available(roomRequest.getAvailable())
                .build();

        Room savedRoom = roomRepository.save(room);
        return mapToRoomResponse(savedRoom);
    }

    // Get all rooms
    @Override
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    // Get a room by ID
    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found with ID: " + id));
        return mapToRoomResponse(room);
    }


    // Update a room
    @Override
    public RoomResponse updateRoom(Long id, RoomRequest roomRequest) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));

        existingRoom.setRoomName(roomRequest.getRoomName());
        existingRoom.setCapacity(roomRequest.getCapacity());
        existingRoom.setFeatures(roomRequest.getFeatures());
        existingRoom.setAvailable(roomRequest.getAvailable());

        Room updatedRoom = roomRepository.save(existingRoom);
        return mapToRoomResponse(updatedRoom);
    }

    // Delete a room
    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));
        roomRepository.delete(room);
    }

    // Get available rooms for booking
    @Override
    public List<RoomResponse> getAvailableRooms() {
        return roomRepository.findByAvailableTrue()
                .stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    // Helper method to map Room entity to RoomResponse DTO
    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .capacity(room.getCapacity())
                .features(room.getFeatures())
                .available(room.getAvailable())
                .build();
    }
}