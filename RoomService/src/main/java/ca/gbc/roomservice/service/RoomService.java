package ca.gbc.roomservice.service;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(RoomRequest roomRequest);
    List<RoomResponse> getAllRooms();
    RoomResponse updateRoom(Long id, RoomRequest roomRequest);
    void deleteRoom(Long id);
    List<RoomResponse> getAvailableRooms();

    RoomResponse getRoomById(Long roomId);
}
