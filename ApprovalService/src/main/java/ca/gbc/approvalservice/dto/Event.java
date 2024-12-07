package ca.gbc.approvalservice.dto;



public record Event(
        String id,
        String organizerId,
        String eventName,
        String eventType,
        String bookingId,
        int expectedAttendees
) {
}
