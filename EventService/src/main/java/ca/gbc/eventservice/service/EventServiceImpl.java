package ca.gbc.eventservice.service;

import ca.gbc.eventservice.dto.Bookings;
import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.dto.Users;
import ca.gbc.eventservice.Client.BookingServiceFeignClient;
import ca.gbc.eventservice.Client.UserServiceFeignClient;
import ca.gbc.eventservice.model.Event;
import ca.gbc.eventservice.repository.EventRepository;
import ca.gbc.eventservice.event.EventPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserServiceFeignClient userServiceFeignClient;
    private final BookingServiceFeignClient bookingServiceFeignClient;
    private final KafkaTemplate<String, EventPlacedEvent> kafkaTemplate;

    private static final Map<String, Integer> ROLE_LIMITATIONS = Map.of(
            "STAFF", 100,
            "FACULTY", 50,
            "STUDENT", 10
    );

    @Autowired
    public EventServiceImpl(EventRepository eventRepository,
                            UserServiceFeignClient userServiceFeignClient,
                            BookingServiceFeignClient bookingServiceFeignClient,
                            KafkaTemplate<String, EventPlacedEvent> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.userServiceFeignClient = userServiceFeignClient;
        this.bookingServiceFeignClient = bookingServiceFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public EventResponse createEvent(EventRequest eventRequest, String authorization) {
        log.info("Creating event with organizer ID: {}", eventRequest.organizerId());

        validateUser(authorization, eventRequest.organizerId());
        validateBookingAndOwnership(authorization, eventRequest.bookingId(), eventRequest.organizerId());
        String userRole = getUserRole(authorization, eventRequest.organizerId());
        validateMaxAttendees(userRole, eventRequest.expectedAttendees());

        Event event = new Event(
                null,
                eventRequest.organizerId(),
                eventRequest.eventName(),
                eventRequest.eventType(),
                eventRequest.bookingId(),
                eventRequest.expectedAttendees()
        );

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());
        String userEmail = eventRequest.userDetails().email();
        EventPlacedEvent eventPlacedEvent = new EventPlacedEvent(savedEvent.getId(), userEmail);
        kafkaTemplate.send("event-placed", eventPlacedEvent.getEventId(), eventPlacedEvent);
        log.info("Published event placed message for Event ID: {}", savedEvent.getId());

        return mapToResponse(savedEvent);
    }


    @Override
    public EventResponse updateEvent(String id, EventRequest eventRequest, String authorization) {
        log.info("Updating event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        validateUser(authorization, event.getOrganizerId());
        validateBooking(authorization, eventRequest.bookingId());
        String userRole = getUserRole(authorization, event.getOrganizerId());
        validateMaxAttendees(userRole, eventRequest.expectedAttendees());

        event.setEventName(eventRequest.eventName());
        event.setEventType(eventRequest.eventType());
        event.setBookingId(eventRequest.bookingId());
        event.setExpectedAttendees(eventRequest.expectedAttendees());

        Event updatedEvent = eventRepository.save(event);
        log.info("Event with ID: {} updated successfully", updatedEvent.getId());

        return mapToResponse(updatedEvent);
    }

    @Override
    public EventResponse getEventById(String id) {
        log.info("Fetching event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        return mapToResponse(event);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        log.info("Fetching all events");

        List<EventResponse> events = eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Total events fetched: {}", events.size());

        return events;
    }

    @Override
    public void deleteEvent(String id, String authorization) {
        log.info("Deleting event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        String userRole = getUserRole(authorization, event.getOrganizerId());
        if (!"ROLE_ADMIN".equals(userRole) &&
                !event.getOrganizerId().equals(getUserIdFromBooking(authorization, event.getBookingId()))) {
            throw new AccessDeniedException("You do not have permission to delete this event.");
        }

        eventRepository.deleteById(id);
        log.info("Event with ID: {} deleted successfully", id);
    }

    private void validateUser(String authorization, String userId) {
        try {
            userServiceFeignClient.getUserById(authorization, userId);
            log.info("User ID: {} validated successfully", userId);
        } catch (Exception e) {
            log.error("User validation failed for User ID: {}", userId, e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid User ID: " + userId, e);
        }
    }

    private void validateBooking(String authorization, String bookingId) {
        try {
            bookingServiceFeignClient.getBookingById(authorization, bookingId);
            log.info("Booking ID: {} validated successfully", bookingId);
        } catch (Exception e) {
            log.error("Booking validation failed for Booking ID: {}", bookingId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Booking ID: " + bookingId, e);
        }
    }

    private void validateBookingAndOwnership(String authorization, String bookingId, String organizerId) {
        try {
            Bookings booking = bookingServiceFeignClient.getBookingById(authorization, bookingId);
            if (!booking.userId().equals(organizerId)) {
                log.error("Booking ID: {} does not belong to Organizer ID: {}", bookingId, organizerId);
                throw new AccessDeniedException("Booking does not belong to the organizer.");
            }
            log.info("Booking ID: {} belongs to Organizer ID: {}", bookingId, organizerId);
        } catch (Exception e) {
            log.error("Validation failed for Booking ID: {} and Organizer ID: {}", bookingId, organizerId, e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Booking or Organizer mismatch", e);
        }
    }

    private String getUserRole(String authorization, String userId) {
        try {
            Users user = userServiceFeignClient.getUserById(authorization, userId);
            return user.role();
        } catch (Exception e) {
            log.error("Failed to fetch user role for User ID: {}", userId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid User ID: " + userId, e);
        }
    }

    private String getUserIdFromBooking(String authorization, String bookingId) {
        try {
            Bookings booking = bookingServiceFeignClient.getBookingById(authorization, bookingId);
            return booking.userId();
        } catch (Exception e) {
            log.error("Failed to fetch user ID from booking with ID: {}", bookingId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Booking ID: " + bookingId, e);
        }
    }

    private void validateMaxAttendees(String userRole, int expectedAttendees) {
        Integer maxAttendees = ROLE_LIMITATIONS.get(userRole);
        if (maxAttendees == null || expectedAttendees > maxAttendees) {
            throw new AccessDeniedException("User role does not allow organizing an event of this size.");
        }
    }

    private EventResponse mapToResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getEventType(),
                event.getBookingId(),
                event.getOrganizerId(),
                event.getExpectedAttendees()
        );
    }
}
