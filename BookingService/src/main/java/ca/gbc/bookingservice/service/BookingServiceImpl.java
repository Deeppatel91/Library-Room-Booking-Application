package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.Transporter.ServiceHandler;
import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceHandler serviceHandler;

    public BookingServiceImpl(BookingRepository bookingRepository, ServiceHandler serviceHandler) {
        this.bookingRepository = bookingRepository;
        this.serviceHandler = serviceHandler;
    }


    public boolean hasConflictingBooking(String userId, String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookingsByUser(userId, roomId, startTime, endTime);
        return !conflictingBookings.isEmpty();
    }

    @Override
    public BookingResponse createBooking(BookingRequest bookingRequest, String userId) {
        // Validate room availability
        validateRoom(bookingRequest.roomId());

        // Check for conflicting bookings by user and room
        if (hasConflictingBooking(userId, bookingRequest.roomId(), bookingRequest.startTime(), bookingRequest.endTime())) {
            throw new IllegalArgumentException("You already have a conflicting booking for this room at the selected time.");
        }

        // Create and save new booking
        Booking booking = new Booking(null, userId, bookingRequest.roomId(),
                bookingRequest.startTime(), bookingRequest.endTime(), bookingRequest.purpose());
        Booking savedBooking = bookingRepository.save(booking);

        return toResponse(savedBooking);
    }


    @Override
    public BookingResponse getBookingById(String id) {
        return bookingRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse updateBooking(String id, BookingRequest bookingRequest, String userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setRoomId(bookingRequest.roomId());
        booking.setStartTime(bookingRequest.startTime());
        booking.setEndTime(bookingRequest.endTime());
        booking.setPurpose(bookingRequest.purpose());

        return toResponse(bookingRepository.save(booking));
    }

    @Override
    public void deleteBooking(String id) {
        bookingRepository.deleteById(id);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
        );
    }

    private void validateRoom(String roomId) {
        if (!serviceHandler.isRoomAvailable(roomId)) {
            throw new IllegalArgumentException("Room is not available");
        }
    }
}