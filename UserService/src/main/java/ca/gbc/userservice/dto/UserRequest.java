package ca.gbc.userservice.dto;

import ca.gbc.userservice.model.Roles;
import ca.gbc.userservice.model.UsersTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String name;
    private String email;
    private String password;
    private Roles role;  // e.g., "student", "staff", "faculty"
    private UsersTypes userType;  // e.g., "student", "staff", "faculty"
}
