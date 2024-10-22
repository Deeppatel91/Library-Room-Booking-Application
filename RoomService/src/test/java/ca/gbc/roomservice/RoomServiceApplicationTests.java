package ca.gbc.roomservice;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RoomServiceApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("password");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void createRoomTest() {
        String requestBody = """
                {
                "roomName" : "Conference Room A",
                "capacity": 50,
                "features" : "Projector, Whiteboard",
                "available" : true
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/rooms")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("roomName", Matchers.equalTo("Conference Room A"))
                .body("capacity", Matchers.equalTo(50))
                .body("features", Matchers.equalTo("Projector, Whiteboard"))
                .body("available", Matchers.equalTo(true));
    }

    @Test
    void getAllRoomsTest() {
        String requestBody = """
            {
            "roomName" : "Conference Room B",
            "capacity": 30,
            "features" : "TV Screen, Sound System",
            "available" : true
            }
            """;

        // Create a room first
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/rooms")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("roomName", Matchers.equalTo("Conference Room B"))
                .body("capacity", Matchers.equalTo(30))
                .body("features", Matchers.equalTo("TV Screen, Sound System"))
                .body("available", Matchers.equalTo(true));

        // Get all rooms and check that the created room exists, regardless of order
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/rooms")
                .then()
                .log().all()
                .statusCode(200)
                .body("roomName", Matchers.hasItems("Conference Room B", "Updated Conference Room", "Conference Room A"))
                .body("capacity", Matchers.hasItems(30, 40, 50));
    }

    @Test
    void updateRoomTest() {
        // First, create a room and get its ID
        String createRequestBody = """
            {
            "roomName" : "Conference Room A",
            "capacity": 50,
            "features" : "Projector, Whiteboard",
            "available" : true
            }
            """;

        // Extract the ID from the creation response
        Integer roomId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/rooms")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .path("id");

        // Then, update the room using the extracted ID
        String updateRequestBody = """
            {
            "roomName" : "Updated Conference Room",
            "capacity": 40,
            "features" : "Updated Features",
            "available" : false
            }
            """;

        RestAssured.given()
                .contentType("application/json")
                .body(updateRequestBody)
                .when()
                .put("/api/rooms/" + roomId)
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    void deleteRoomTest() {
        // First, create a room and get its ID
        String createRequestBody = """
        {
        "roomName" : "Conference Room A",
        "capacity": 50,
        "features" : "Projector, Whiteboard",
        "available" : true
        }
        """;

        Integer roomId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/rooms")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .path("id");

        // Then, delete the room
        RestAssured.given()
                .contentType("application/json")
                .when()
                .delete("/api/rooms/" + roomId)
                .then()
                .log().all()
                .statusCode(204);

        // Verify the room no longer exists
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/rooms/" + roomId)
                .then()
                .log().all()
                .statusCode(404);  // Expecting a 404 Not Found after deletion
    }

}
