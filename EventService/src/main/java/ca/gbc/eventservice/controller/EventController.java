package ca.gbc.eventservice.controller;

import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventRequest eventRequest, Authentication authentication) {
        String organizerId = getUserIdFromAuth(authentication);
        try {
            EventResponse eventResponse = eventService.createEvent(eventRequest, organizerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable String id) {
        log.info("Retrieving event with ID: {}", id);
        try {
            EventResponse eventResponse = eventService.getEventById(id);
            log.info("Event retrieved successfully with ID: {}", id);
            return ResponseEntity.ok(eventResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Event not found for ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found for ID: " + id);
        } catch (Exception e) {
            log.error("An error occurred while retrieving the event with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving the event");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @RequestBody EventRequest eventRequest, Authentication authentication) {
        String organizerId = getUserIdFromAuth(authentication);
        try {
            EventResponse updatedEvent = eventService.updateEvent(id, eventRequest, organizerId);
            return ResponseEntity.ok(updatedEvent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id, Authentication authentication) {
        String organizerId = getUserIdFromAuth(authentication);
        try {
            eventService.deleteEvent(id, organizerId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    private String getUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getPrincipal().toString();
        }
        throw new IllegalArgumentException("Authentication credentials missing");
    }
}
