package ca.gbc.eventservice.dto;


public record EventResponse(
        String id,
        String eventName,
        String eventType,
        String bookingId,
        String organizerId,
        int expectedAttendees
) {}
