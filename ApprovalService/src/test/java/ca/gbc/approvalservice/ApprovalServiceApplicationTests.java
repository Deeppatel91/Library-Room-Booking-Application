package ca.gbc.approvalservice;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.service.ApprovalService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApprovalServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @LocalServerPort
    private Integer port;

    @MockBean
    private ApprovalService approvalService;

    private final String jwtSecret = "775367566B5970743373367639792F423F4528482B4D6251655468576D5A713474";

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("jwt.secret", () -> "775367566B5970743373367639792F423F4528482B4D6251655468576D5A713474");
        registry.add("jwt.expiration", () -> "86400000");
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.defaultParser = Parser.JSON;
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
    void testApproveEvent() {

        String jwtToken = generateJwtToken("admin123", "STAFF");

        ApprovalRequest approvalRequest = new ApprovalRequest("event123", "admin123", "APPROVED", "Approval Comment");
        ApprovalResponse mockResponse = new ApprovalResponse("approvalId", "event123", "admin123", "APPROVED", "Approval Comment", LocalDateTime.now());

        Mockito.when(approvalService.approveEvent(Mockito.any(ApprovalRequest.class), anyString())).thenReturn(mockResponse);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(approvalRequest)
                .when()
                .post("/api/approvals")
                .then()
                .statusCode(200)
                .body("id", equalTo("approvalId"))
                .body("eventId", equalTo("event123"))
                .body("status", equalTo("APPROVED"));
    }

    @Test
    void testGetApprovalById() {
        String jwtToken = generateJwtToken("admin123", "STAFF");

        ApprovalResponse mockResponse = new ApprovalResponse("approvalId", "event123", "admin123", "APPROVED", "Approval Comment", LocalDateTime.now());
        Mockito.when(approvalService.getApprovalById("approvalId")).thenReturn(mockResponse);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/approvals/approvalId")
                .then()
                .statusCode(200)
                .body("id", equalTo("approvalId"))
                .body("eventId", equalTo("event123"))
                .body("status", equalTo("APPROVED"));
    }

    @Test
    void testUpdateApproval() {
        String jwtToken = generateJwtToken("admin123", "STAFF");

        ApprovalRequest updateRequest = new ApprovalRequest("event123", "admin123", "REJECTED", "Updated Comment");
        ApprovalResponse mockUpdatedResponse = new ApprovalResponse("approvalId", "event123", "admin123", "REJECTED", "Updated Comment", LocalDateTime.now());

        Mockito.when(approvalService.updateApproval("approvalId", updateRequest, jwtToken)).thenReturn(mockUpdatedResponse);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/approvals/approvalId")
                .then()
                .statusCode(200);
    }

    @Test
    void testGetAllApprovals() {
        String jwtToken = generateJwtToken("admin123", "STAFF");

        List<ApprovalResponse> mockResponses = List.of(
                new ApprovalResponse("approvalId1", "event123", "admin123", "APPROVED", "Approval Comment 1", LocalDateTime.now()),
                new ApprovalResponse("approvalId2", "event456", "admin123", "PENDING", "Approval Comment 2", LocalDateTime.now())
        );

        Mockito.when(approvalService.getAllApprovals()).thenReturn(mockResponses);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/approvals/all")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }



    @Test
    void testDeleteApproval() {
        String jwtToken = generateJwtToken("admin123", "STAFF");

        Mockito.doNothing().when(approvalService).deleteApproval("approvalId");

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete("/api/approvals/approvalId")
                .then()
                .statusCode(204);
    }
}
