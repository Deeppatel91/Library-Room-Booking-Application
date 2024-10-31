package ca.gbc.bookingservice.repository;


import ca.gbc.bookingservice.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {


    @Query("{'roomId': ?0, 'startDate': {$lt: ?2}, 'endDate': {$gt: ?1}}")
    List<Booking> findConflictingBookings(String roomId, LocalDateTime startDate, LocalDateTime endDate);
}