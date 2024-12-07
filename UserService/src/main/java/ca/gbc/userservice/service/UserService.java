package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest userRequest); // Create a user
    UserResponse getUserById(Long id); // Get a user by ID
    List<UserResponse> getAllUsers(); // Get all users
    UserResponse updateUser(Long id, UserRequest userRequest); // Update a user
    void deleteUser(Long id); // Delete a user
    UserResponse deactivateUser(Long id); // Deactivate a user
    UserResponse activateUser(Long id); // Activate a user
    UserResponse logIn(String email, String password); // Log in a user
}
