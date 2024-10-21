package ca.gbc.roomservice.repository;

import ca.gbc.roomservice.model.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Repository
public class RoomRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Room> roomMapper = (ResultSet rs, int rowNum) -> {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setRoomName(rs.getString("room_name"));
        room.setCapacity(rs.getInt("capacity"));
        room.setFeatures(rs.getString("features"));
        room.setAvailable(rs.getBoolean("available"));
        return room;
    };

    public List<Room> findAllAvailableRooms() {
        String sql = "SELECT * FROM rooms WHERE available = TRUE";
        return jdbcTemplate.query(sql, roomMapper);
    }

    public Optional<Room> findById(Long id) {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        List<Room> rooms = jdbcTemplate.query(sql, roomMapper, id);
        return rooms.stream().findFirst();
    }

    public int save(Room room) {
        String sql = "INSERT INTO rooms (room_name, capacity, features, available) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, room.getRoomName(), room.getCapacity(), room.getFeatures(), room.getAvailable());
    }

    public int update(Room room) {
        String sql = "UPDATE rooms SET room_name = ?, capacity = ?, features = ?, available = ? WHERE id = ?";
        return jdbcTemplate.update(sql, room.getRoomName(), room.getCapacity(), room.getFeatures(), room.getAvailable(), room.getId());
    }

    public int delete(Long id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
