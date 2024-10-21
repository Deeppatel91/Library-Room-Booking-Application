package ca.gbc.roomservice.service;

import ca.gbc.roomservice.model.Room;
import ca.gbc.roomservice.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public List<Room> findAllAvailableRooms() {
        return roomRepository.findAllAvailableRooms();
    }

    @Override
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    public Room save(Room room) {
        int result = roomRepository.save(room);
        if (result > 0) {
            return room;
        } else {
            throw new RuntimeException("Failed to save the room");
        }
    }

    @Override
    public Room update(Room room) {
        int result = roomRepository.update(room);
        if (result > 0) {
            return room;
        } else {
            throw new RuntimeException("Failed to update the room");
        }
    }

    @Override
    public boolean delete(Long id) {
        int result = roomRepository.delete(id);
        return result > 0;
    }
}
