package ca.gbc.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizationResponse {
    private String token;  // JWT token
    private String email;  // User's email
    private String role;   // e.g., "student", "staff", "faculty"
    private String userType;  // e.g., "student", "staff", "faculty"
}
