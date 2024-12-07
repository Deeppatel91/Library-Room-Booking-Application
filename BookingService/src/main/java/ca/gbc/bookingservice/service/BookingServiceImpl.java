package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.Client.RoomServiceFeignClient;
import ca.gbc.bookingservice.Client.UserServiceFeignClient;
import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.event.BookingPlacedEvent;
import ca.gbc.bookingservice.exception.BookingServiceException;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final RoomServiceFeignClient roomServiceClient;
    private final UserServiceFeignClient userServiceClient;
    private final KafkaTemplate<String, BookingPlacedEvent> kafkaTemplate;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              RoomServiceFeignClient roomServiceClient,
                              UserServiceFeignClient userServiceClient,
                              KafkaTemplate<String, BookingPlacedEvent> kafkaTemplate) {
        this.bookingRepository = bookingRepository;
        this.roomServiceClient = roomServiceClient;
        this.userServiceClient = userServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    @Override
    public BookingResponse createBooking(BookingRequest bookingRequest, String authorization) {
        log.info("Attempting to create booking for User ID: {} with Room ID: {}", bookingRequest.userId(), bookingRequest.roomId());
        try {
            validateUser(authorization, bookingRequest.userId());
            Long roomId = parseRoomId(bookingRequest.roomId());
            validateRoom(roomId);

            List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                    bookingRequest.roomId(),
                    bookingRequest.startTime(),
                    bookingRequest.endTime()
            );

            if (!conflictingBookings.isEmpty()) {
                log.warn("Room ID: {} is already booked during the requested time", bookingRequest.roomId());
                throw BookingServiceException.roomAlreadyOccupied("Room is already booked during the requested time.");
            }

            Booking booking = new Booking(
                    null,
                    bookingRequest.userId(),
                    bookingRequest.roomId(),
                    bookingRequest.startTime(),
                    bookingRequest.endTime(),
                    bookingRequest.purpose()
            );
            Booking savedBooking = bookingRepository.save(booking);

            log.info("Booking created successfully with ID: {}", savedBooking.getId());

            String userEmail = bookingRequest.userDetails().email();
            BookingPlacedEvent event = new BookingPlacedEvent(
                    savedBooking.getId(),
                    userEmail
            );
            kafkaTemplate.send("booking-placed", event.getBookingId(), event);
            log.info("Booking event published for ID: {}", savedBooking.getId());

            return mapToResponse(savedBooking);

        } catch (BookingServiceException ex) {
            log.error("Error during booking creation: {}", ex.getReason(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during booking creation: {}", ex.getMessage(), ex);
            throw BookingServiceException.unexpectedError("Unexpected error occurred during booking creation", ex);
        }
    }

    @Override
    public BookingResponse updateBooking(String id, BookingRequest bookingRequest, String authorization) {
        log.info("Updating booking with ID: {}", id);

        try {
            validateUser(authorization, bookingRequest.userId());

            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> BookingServiceException.bookingNotFound("Booking not found with ID: " + id));

            booking.setRoomId(bookingRequest.roomId());
            booking.setStartTime(bookingRequest.startTime());
            booking.setEndTime(bookingRequest.endTime());
            booking.setPurpose(bookingRequest.purpose());

            Booking updatedBooking = bookingRepository.save(booking);
            log.info("Booking with ID: {} updated successfully", updatedBooking.getId());
            return mapToResponse(updatedBooking);

        } catch (BookingServiceException ex) {
            log.error("Error during booking update: {}", ex.getReason(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during booking update: {}", ex.getMessage(), ex);
            throw BookingServiceException.unexpectedError("Unexpected error occurred during booking update", ex);
        }
    }

    @Override
    public BookingResponse getBookingById(String id) {
        log.info("Fetching booking with ID: {}", id);

        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> BookingServiceException.bookingNotFound("Booking not found with ID: " + id));
            log.info("Booking with ID: {} fetched successfully", id);
            return mapToResponse(booking);

        } catch (BookingServiceException ex) {
            log.error("Error fetching booking: {}", ex.getReason(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching booking: {}", ex.getMessage(), ex);
            throw BookingServiceException.unexpectedError("Unexpected error occurred while fetching booking", ex);
        }
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        log.info("Fetching all bookings");

        try {
            List<BookingResponse> bookings = bookingRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            log.info("Total bookings fetched: {}", bookings.size());
            return bookings;

        } catch (Exception ex) {
            log.error("Unexpected error fetching bookings: {}", ex.getMessage(), ex);
            throw BookingServiceException.unexpectedError("Unexpected error occurred while fetching bookings", ex);
        }
    }

    @Override
    public void deleteBooking(String id) {
        log.info("Deleting booking with ID: {}", id);

        try {
            if (!bookingRepository.existsById(id)) {
                log.error("Booking with ID: {} not found", id);
                throw BookingServiceException.bookingNotFound("Booking not found with ID: " + id);
            }

            bookingRepository.deleteById(id);
            log.info("Booking with ID: {} deleted successfully", id);

        } catch (BookingServiceException ex) {
            log.error("Error during booking deletion: {}", ex.getReason(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during booking deletion: {}", ex.getMessage(), ex);
            throw BookingServiceException.unexpectedError("Unexpected error occurred during booking deletion", ex);
        }
    }

    private void validateUser(String authorization, String userId) {
        try {
            log.info("Validating user with ID: {}", userId);
            userServiceClient.getUserById(authorization, Long.parseLong(userId));
            log.info("User with ID: {} validated successfully", userId);
        } catch (Exception e) {
            log.error("User validation failed for ID: {}", userId, e);
            throw BookingServiceException.userAuthenticationFailed("User not found or unauthorized for ID: " + userId);
        }
    }

    private Long parseRoomId(String roomId) {
        try {
            return Long.valueOf(roomId);
        } catch (NumberFormatException e) {
            log.error("Invalid room ID format: {}", roomId, e);
            throw BookingServiceException.roomNotFound("Invalid room ID format: " + roomId);
        }
    }

    private void validateRoom(Long roomId) {
        try {
            log.info("Validating room with ID: {}", roomId);
            roomServiceClient.getRoomById(roomId);  // Assuming the ID is already a Long
            log.info("Room with ID: {} validated successfully", roomId);
        } catch (Exception e) {
            log.error("Room validation failed for ID: {}, error: {}", roomId, e.getMessage());
            throw BookingServiceException.serviceUnavailable("Service Unavailable");
        }
    }



    private BookingResponse mapToResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
        );
    }
}
