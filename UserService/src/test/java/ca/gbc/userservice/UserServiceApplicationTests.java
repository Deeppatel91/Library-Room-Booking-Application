package ca.gbc.userservice;

import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.model.Roles;
import ca.gbc.userservice.model.Users;
import ca.gbc.userservice.model.UsersTypes;
import ca.gbc.userservice.repository.UsersRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @LocalServerPort
    private Integer port;

    @Autowired
    private UsersRepository usersRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("jwt.secret", () -> "775367566B5970743373367639792F423F4528482B4D6251655468576D5A713474");
        registry.add("jwt.expiration", () -> 3600);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        if (usersRepository.findByEmail("admin@example.com").isEmpty()) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode("password");

            Users adminUser = new Users(null, "Admin", "admin@example.com", encodedPassword,
                    Roles.ADMIN, UsersTypes.STAFF, true);
            usersRepository.save(adminUser);
        }
    }

    private String authenticateAndGetJwtToken() {
        AuthorizationRequest authRequest = new AuthorizationRequest("admin@example.com", "password");

        return given()
                .contentType(ContentType.JSON)
                .body(authRequest)
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    void testCreateUser() {
        String jwtToken = authenticateAndGetJwtToken();
        String uniqueEmail = "john.doe." + System.currentTimeMillis() + "@example.com";

        UserRequest userRequest = new UserRequest(
                "John Doe",
                uniqueEmail,
                "password123",
                Roles.STAFF,
                UsersTypes.STAFF
        );

        ValidatableResponse response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(userRequest)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201);

        response.body("id", notNullValue())
                .body("name", equalTo("John Doe"))
                .body("email", equalTo(uniqueEmail))
                .body("role", equalTo("STAFF"))
                .body("userType", equalTo("STAFF"));
    }

    @Test
    void testGetUserById() {
        String jwtToken = authenticateAndGetJwtToken();
        Users user = usersRepository.findByEmail("admin@example.com").orElseThrow();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/users/" + user.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(user.getId().intValue()))
                .body("email", equalTo("admin@example.com"));
    }

    @Test
    void testUpdateUser() {
        String jwtToken = authenticateAndGetJwtToken();

        Users user = usersRepository.findByEmail("admin@example.com").orElseThrow();

        UserRequest userRequest = new UserRequest(
                "Updated Name",
                "updated.email@example.com",
                "newpassword123",
                Roles.STAFF,
                UsersTypes.STAFF
        );

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(userRequest)
                .when()
                .put("/api/users/" + user.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Name"));
    }

    @Test
    void testDeleteUser() {
        String jwtToken = authenticateAndGetJwtToken();

        Users user = usersRepository.findByEmail("admin@example.com").orElseThrow();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete("/api/users/" + user.getId())
                .then()
                .statusCode(204);
    }

    @Test
    void testDeactivateUser() {
        String jwtToken = authenticateAndGetJwtToken();

        Users user = usersRepository.findByEmail("admin@example.com").orElseThrow();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .put("/api/users/Deactivate/" + user.getId())
                .then()
                .statusCode(200)
                .body("active", equalTo(false));
    }



    @Test
    void testChangeUserRole() {
        String jwtToken = authenticateAndGetJwtToken();

        Users user = usersRepository.findByEmail("admin@example.com").orElseThrow();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .param("role", "ADMIN")
                .when()
                .put("/api/users/Role/" + user.getId())
                .then()
                .statusCode(200)
                .body("role", equalTo("ADMIN"));
    }
    @Test
    void testActivateUser() {
        String jwtToken = authenticateAndGetJwtToken();

        Users user = usersRepository.findByEmail("admin@example.com").orElseThrow();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .put("/api/users/Activate/" + user.getId())
                .then()
                .statusCode(200)
                .body("active", equalTo(true));
    }

    @Test
    void testCreateAuthenticationToken() {
        AuthorizationRequest authRequest = new AuthorizationRequest("admin@example.com", "password");

        given()
                .contentType(ContentType.JSON)
                .body(authRequest)
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }
}
