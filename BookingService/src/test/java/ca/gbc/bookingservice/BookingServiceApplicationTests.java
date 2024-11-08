package ca.gbc.bookingservice;

import ca.gbc.bookingservice.Client.RoomServiceFeignClient;
import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
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

import java.time.LocalDateTime;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookingServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @LocalServerPort
    private Integer port;

    @Autowired
    private BookingRepository bookingRepository;

    private final String jwtSecret = "775367566B5970743373367639792F423F4528482B4D6251655468576D5A713474";

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("jwt.secret", () -> "775367566B5970743373367639792F423F4528482B4D6251655468576D5A713474");
        registry.add("ROOM_SERVICE_URL", () -> "http://localhost:8090");  // Set to localhost for testing
    }

    @MockBean
    private RoomServiceFeignClient roomServiceFeignClient;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        bookingRepository.deleteAll();

        Mockito.when(roomServiceFeignClient.getRoomById(Mockito.anyString())).thenReturn("Available");
    }

    private String generateJwtToken(String userId) {
        byte[] keyBytes = hexStringToByteArray(jwtSecret);
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))
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
    void testCreateBooking() {
        String jwtToken = generateJwtToken("user123");

        BookingRequest bookingRequest = new BookingRequest(
                "user123",
                "room123",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "Meeting"
        );

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(bookingRequest)
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("userId", equalTo("user123"))
                .body("roomId", equalTo("room123"))
                .body("purpose", equalTo("Meeting"));
    }

    @Test
    void testGetBookingById() {
        String jwtToken = generateJwtToken("user123");
        Booking booking = bookingRepository.save(new Booking(
                null,
                "user123",
                "room123",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "Meeting"
        ));

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/bookings/" + booking.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(booking.getId()))
                .body("userId", equalTo("user123"))
                .body("roomId", equalTo("room123"))
                .body("purpose", equalTo("Meeting"));
    }

    @Test
    void testGetAllBookings() {
        bookingRepository.save(new Booking(null, "user123", "room123", LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Meeting"));
        bookingRepository.save(new Booking(null, "user456", "room456", LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3), "Training"));

        given()
                .header("Authorization", "Bearer " + generateJwtToken("user123"))
                .when()
                .get("/api/bookings/all")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(1));
    }

    @Test
    void testUpdateBooking() {
        String jwtToken = generateJwtToken("user123");
        Booking booking = bookingRepository.save(new Booking(
                null,
                "user123",
                "room123",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "Meeting"
        ));

        BookingRequest updatedRequest = new BookingRequest(
                "user123",
                "room123",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                "Updated Meeting"
        );

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(updatedRequest)
                .when()
                .put("/api/bookings/" + booking.getId())
                .then()
                .statusCode(200)
                .body("purpose", equalTo("Updated Meeting"))
                .body("endTime", notNullValue());
    }

    @Test
    void testDeleteBooking() {
        String jwtToken = generateJwtToken("user123");
        Booking booking = bookingRepository.save(new Booking(
                null,
                "user123",
                "room123",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "Meeting"
        ));

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete("/api/bookings/" + booking.getId())
                .then()
                .statusCode(204);
    }
}
