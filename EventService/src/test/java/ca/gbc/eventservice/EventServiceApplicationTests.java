package ca.gbc.eventservice;

import ca.gbc.eventservice.Transporter.BookingServiceFeignClient;
import ca.gbc.eventservice.Transporter.UserServiceFeignClient;
import ca.gbc.eventservice.dto.Bookings;
import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.dto.Users;
import ca.gbc.eventservice.repository.EventRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EventServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @LocalServerPort
    private Integer port;

    @Autowired
    private EventRepository eventRepository;

    private final String jwtSecret = "775367566B5970743373367639792F423F4528482B4D6251655468576D5A713474";

    @MockBean
    private BookingServiceFeignClient bookingServiceFeignClient;

    @MockBean
    private UserServiceFeignClient userServiceFeignClient;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("jwt.secret", () -> "775367566B5970743373367639792F423F4528482B4D6251655468576D5A713474");
        registry.add("jwt.expiration", () -> "86400000"); // 24 hours in milliseconds
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        eventRepository.deleteAll();

        // Mock responses for BookingService and UserService
        Mockito.when(bookingServiceFeignClient.getBookingById(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new Bookings("bookingId", "organizer123@example.com", "roomId"));
        Mockito.when(userServiceFeignClient.getUserById(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new Users("organizer123", "STAFF", "organizer123@example.com"));
    }

    private String generateJwtToken(String userId, String role) {
        byte[] keyBytes = hexStringToByteArray(jwtSecret);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1-hour expiration
                .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Test
    void testCreateEvent() {
        String jwtToken = generateJwtToken("organizer123", "STAFF");

        EventRequest eventRequest = new EventRequest(
                "Sample Event",
                "Conference",
                "bookingId",
                100
        );

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(eventRequest)
                .when()
                .post("/api/events")
                .then()
                .statusCode(201)
                .body("eventName", equalTo("Sample Event"))
                .body("eventType", equalTo("Conference"));
    }

    @Test
    void testGetEventById() {
        String jwtToken = generateJwtToken("organizer123", "STAFF");

        // First, create an event to retrieve later
        EventRequest eventRequest = new EventRequest("Sample Event", "Conference", "bookingId", 100);
        EventResponse eventResponse = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(eventRequest)
                .when()
                .post("/api/events")
                .as(EventResponse.class);

        String eventId = eventResponse.id(); // Corrected to `id()`

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/events/" + eventId)
                .then()
                .statusCode(200)
                .body("eventName", equalTo("Sample Event"));
    }

    @Test
    void testGetAllEvents() {
        String jwtToken = generateJwtToken("organizer123", "STAFF");

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/events/all")
                .then()
                .statusCode(200);
    }

    @Test
    void testUpdateEvent() {
        String jwtToken = generateJwtToken("organizer123", "STAFF");

        EventRequest createRequest = new EventRequest("Sample Event", "Conference", "bookingId", 100);
        EventResponse createdEvent = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/api/events")
                .as(EventResponse.class);

        EventRequest updateRequest = new EventRequest("Updated Event", "Workshop", "bookingId", 50);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/events/" + createdEvent.id()) // Corrected to `id()`
                .then()
                .statusCode(200)
                .body("eventName", equalTo("Updated Event"));
    }

    @Test
    void testDeleteEvent() {
        String jwtToken = generateJwtToken("organizer123", "STAFF");

        EventRequest createRequest = new EventRequest("Sample Event", "Conference", "bookingId", 100);
        EventResponse createdEvent = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/api/events")
                .as(EventResponse.class);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete("/api/events/" + createdEvent.id()) // Corrected to `id()`
                .then()
                .statusCode(204);
    }
}
