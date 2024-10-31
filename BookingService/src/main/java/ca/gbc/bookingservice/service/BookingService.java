package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, String userId);
    BookingResponse updateBooking(String id, BookingRequest request, String userId);
    void deleteBooking(String id);
    BookingResponse getBookingById(String id);
    List<BookingResponse> getAllBookings();
}
