package ca.gbc.roomservice.service;

import ca.gbc.roomservice.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {

    /**
     * Retrieves all available rooms.
     *
     * @return a list of all available rooms.
     */
    List<Room> findAllAvailableRooms();

    /**
     * Finds a room by its ID.
     *
     * @param id the ID of the room.
     * @return an Optional containing the found room, or an empty Optional if no room is found.
     */
    Optional<Room> findById(Long id);

    /**
     * Adds a new room to the database.
     *
     * @param room the room to add.
     * @return the newly added room.
     */
    Room save(Room room);

    /**
     * Updates details of an existing room.
     *
     * @param room the room with updated details.
     * @return the updated room.
     */
    Room update(Room room);

    /**
     * Deletes a room by its ID.
     *
     * @param id the ID of the room to delete.
     * @return true if the room was successfully deleted, false otherwise.
     */
    boolean delete(Long id);
}
