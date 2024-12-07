package ca.gbc.userservice.dto;

import ca.gbc.userservice.model.Roles;
import ca.gbc.userservice.model.UsersTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String password;
    private Roles role;
    private UsersTypes userType;
}
