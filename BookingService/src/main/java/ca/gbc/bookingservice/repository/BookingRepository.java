package ca.gbc.bookingservice.repository;

import ca.gbc.bookingservice.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    @Query("{ 'roomId': ?0, $or: [ " +
            "{ 'startTime': { $lt: ?2, $gte: ?1 } }, " +
            "{ 'endTime': { $lte: ?2, $gt: ?1 } }, " +
            "{ 'startTime': { $lte: ?1 }, 'endTime': { $gte: ?2 } } " +
            "] }")
    List<Booking> findConflictingBookings(String roomId, LocalDateTime startTime, LocalDateTime endTime);

}
