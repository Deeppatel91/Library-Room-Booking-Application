package ca.gbc.eventservice.service;

import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;

import java.util.List;

public interface EventService {
    EventResponse createEvent(EventRequest eventRequest, String organizerId);
    EventResponse getEventById(String id);
    List<EventResponse> getAllEvents();
    EventResponse updateEvent(String id, EventRequest eventRequest, String organizerId);
    void deleteEvent(String id, String organizerId);
}
