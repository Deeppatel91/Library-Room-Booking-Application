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
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Roles role;
    private UsersTypes userType;
    private boolean active;


}
