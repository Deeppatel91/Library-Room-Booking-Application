package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.model.Roles;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService{
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(Long userId);
    UserResponse updateUser(Long userId, UserRequest request);
    void deleteUser(Long userId);
    UserResponse deactivateUser(Long userId);
    UserResponse activateUser(Long userId);


    UserResponse changeUserRole(Long userId, Roles role);

    UserDetails loadUserByUsername(String email);

    boolean noAdminExists();

    Object changeUserRole(Long userId, String name);
}
