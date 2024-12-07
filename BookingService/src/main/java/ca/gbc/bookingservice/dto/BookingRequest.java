package ca.gbc.bookingservice.dto;

import java.time.LocalDateTime;

public record BookingRequest(
        String userId,
        String roomId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String purpose,
        UserDetails userDetails
) {
    public record UserDetails(String email, String firstName, String lastName) {}
}
