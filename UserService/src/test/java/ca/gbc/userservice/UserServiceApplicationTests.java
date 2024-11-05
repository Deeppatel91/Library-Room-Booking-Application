package ca.gbc.userservice;

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
class UserServiceApplicationTests {

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
    void createUserTest() {
        String requestBody = """
                {
                "name": "John Doe",
                "email": "john@example.com",
                "password": "password123",
                "role": "STAFF",
                "userType": "STAFF"
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/users/init")
                .then()
                .log().all()
                .statusCode(200)
                .body("name", Matchers.equalTo("John Doe"))
                .body("email", Matchers.equalTo("john@example.com"))
                .body("role", Matchers.equalTo("STAFF"))
                .body("active", Matchers.equalTo(true));
    }

    @Test
    void loginUserTest() {
        String requestBody = """
                {
                "email": "john@example.com",
                "password": "password123"
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/users/login")
                .then()
                .log().all()
                .statusCode(200)
                .body("token", Matchers.notNullValue());
    }

    @Test
    void getUserByIdTest() {
        String createRequestBody = """
                {
                "name": "Jane Doe",
                "email": "jane@example.com",
                "password": "password456",
                "role": "STUDENT",
                "userType": "STUDENT"
                }
                """;

        Integer userId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/users/init")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("id");

        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/users/" + userId)
                .then()
                .log().all()
                .statusCode(200)
                .body("name", Matchers.equalTo("Jane Doe"))
                .body("email", Matchers.equalTo("jane@example.com"));
    }

    @Test
    void updateUserTest() {
        String createRequestBody = """
                {
                "name": "Mark Smith",
                "email": "mark@example.com",
                "password": "password789",
                "role": "STAFF",
                "userType": "STAFF"
                }
                """;

        Integer userId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/users/init")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("id");

        String updateRequestBody = """
                {
                "name": "Mark Updated",
                "email": "markupdated@example.com",
                "password": "newpassword",
                "role": "STAFF",
                "userType": "STAFF"
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(updateRequestBody)
                .when()
                .put("/api/users/" + userId)
                .then()
                .log().all()
                .statusCode(200)
                .body("name", Matchers.equalTo("Mark Updated"))
                .body("email", Matchers.equalTo("markupdated@example.com"));
    }

    @Test
    void deleteUserTest() {
        String createRequestBody = """
                {
                "name": "Lucas Doe",
                "email": "lucas@example.com",
                "password": "password987",
                "role": "STUDENT",
                "userType": "STUDENT"
                }
                """;

        Integer userId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/users/init")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("id");

        RestAssured.given()
                .contentType("application/json")
                .when()
                .delete("/api/users/" + userId)
                .then()
                .log().all()
                .statusCode(204);

        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/users/" + userId)
                .then()
                .log().all()
                .statusCode(404);
    }

    @Test
    void activateUserTest() {
        String createRequestBody = """
                {
                "name": "Alice Johnson",
                "email": "alice@example.com",
                "password": "password321",
                "role": "ADMIN",
                "userType": "STAFF"
                }
                """;

        Integer userId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/users/init")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("id");

        RestAssured.given()
                .contentType("application/json")
                .when()
                .put("/api/users/deactivate/" + userId)
                .then()
                .statusCode(200);

        RestAssured.given()
                .contentType("application/json")
                .when()
                .put("/api/users/activate/" + userId)
                .then()
                .log().all()
                .statusCode(200)
                .body("active", Matchers.equalTo(true));
    }
}
