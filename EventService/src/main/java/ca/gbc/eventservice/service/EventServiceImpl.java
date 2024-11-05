package ca.gbc.eventservice.service;

import ca.gbc.eventservice.Transporter.BookingServiceFeignClient;
import ca.gbc.eventservice.Transporter.UserServiceFeignClient;
import ca.gbc.eventservice.dto.Bookings;
import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.dto.Users;
import ca.gbc.eventservice.model.Event;
import ca.gbc.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserServiceFeignClient userServiceFeignClient;
    private final BookingServiceFeignClient bookingServiceFeignClient;

    private static final Map<String, Integer> ROLE_MAX_ATTENDEES = Map.of(
            "ADMIN", 700,
            "STAFF", 400,
            "FACULTY", 300,
            "STUDENT", 50
    );

    @Override
    public EventResponse createEvent(EventRequest eventRequest, String organizerId) {
        String jwtToken = extractJwtToken();
        validateBooking(eventRequest.bookingId(), organizerId, jwtToken);

        Users user = userServiceFeignClient.getUserById(organizerId, jwtToken);
        validateEventSize(user.role(), eventRequest.expectedAttendees());

        Event event = new Event(
                null,
                organizerId,
                eventRequest.eventName(),
                eventRequest.eventType(),
                eventRequest.bookingId(),
                eventRequest.expectedAttendees()
        );

        return mapToResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse updateEvent(String id, EventRequest eventRequest, String organizerId) {
        String jwtToken = extractJwtToken();
        validateBooking(eventRequest.bookingId(), organizerId, jwtToken);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new MissingEventException("Event not found with ID: " + id));

        Users user = userServiceFeignClient.getUserById(organizerId, jwtToken);
        validateEventSize(user.role(), eventRequest.expectedAttendees());

        event.setEventName(eventRequest.eventName());
        event.setEventType(eventRequest.eventType());
        event.setBookingId(eventRequest.bookingId());
        event.setExpectedAttendees(eventRequest.expectedAttendees());

        return mapToResponse(eventRepository.save(event));
    }

    private void validateEventSize(String role, int expectedAttendees) {
        role = role.toUpperCase();
        Integer maxAttendees = ROLE_MAX_ATTENDEES.get(role);

        if (maxAttendees == null) {
            log.error("No attendee limit configured for role '{}'.", role);
            throw new IllegalStateException("No attendee limit configured for role: " + role);
        }
        if (maxAttendees < expectedAttendees) {
            log.error("User with role '{}' attempted to create an event with {} attendees (limit: {}).", role, expectedAttendees, maxAttendees);
            throw new AccessDeniedException("User role limitations exceeded for attendee count.");
        }
    }

    @Override
    public EventResponse getEventById(String id) {
        return eventRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new MissingEventException("Event not found with ID: " + id));
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEvent(String id, String organizerId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new MissingEventException("Event not found with ID: " + id));

        if (!event.getOrganizerId().equals(organizerId)) {
            log.error("Organizer ID mismatch: user {} tried to delete event organized by {}.", organizerId, event.getOrganizerId());
            throw new AccessDeniedException("You don't have permission to delete this event.");
        }

        eventRepository.deleteById(id);
    }

    private void validateBooking(String bookingId, String organizerId, String jwtToken) {
        String bearerToken = formatBearerToken(jwtToken);
        Bookings booking;
        Users organizer;

        try {
            booking = bookingServiceFeignClient.getBookingById(bookingId, bearerToken);
            organizer = userServiceFeignClient.getUserById(organizerId, bearerToken);
        } catch (Exception e) {
            log.error("Error fetching booking or user data with token {}: {}", bearerToken, e.getMessage());
            throw new IllegalStateException("Failed to validate booking or user data");
        }

        if (!booking.userId().equals(organizer.email())) {
            log.error("Organizer ID '{}' does not match the User ID '{}' in the booking for booking ID '{}'.", organizer.email(), booking.userId(), bookingId);
            throw new AccessDeniedException("The organizer ID does not match the user ID in the booking.");
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

    private String extractJwtToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            String token = auth.getCredentials().toString();
            log.info("JWT token retrieved from SecurityContext.");
            return formatBearerToken(token);
        } else {
            log.error("JWT token missing in SecurityContext for authentication.");
            throw new IllegalArgumentException("No JWT token found for authorization");
        }
    }

    private String formatBearerToken(String token) {
        return token != null && !token.trim().startsWith("Bearer ") ? "Bearer " + token.trim() : token;
    }
}
