package ca.gbc.eventservice.dto;

public record EventRequest(
        String eventName,
        String eventType,
        String bookingId,
        int expectedAttendees
) {}
