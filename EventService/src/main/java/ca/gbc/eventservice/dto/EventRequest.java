package ca.gbc.eventservice.dto;

public record EventRequest(
        String organizerId,
        String eventName,
        String eventType,
        String bookingId,
        int expectedAttendees,
        UserDetails userDetails
) {
    public record UserDetails(String email, String firstName, String lastName) {}
}
