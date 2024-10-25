package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.AuthorizationRequest;
import ca.gbc.userservice.dto.AuthorizationResponse;
import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;

public interface UserService {

    /**
     * Register a new user
     */
    UserResponse registerUser(UserRequest userRequest);

    /**
     * Authenticate the user and generate a JWT token
     */
    AuthorizationResponse authenticateUser(AuthorizationRequest authorizationRequest);

    /**
     * Update user's own profile or allow admin to update any profile
     */
    UserResponse updateUser(Long id, UserRequest userRequest, String token);

    /**
     * Delete user's own profile or allow admin to delete any profile
     */
    void deleteUser(Long id, String token);

    /**
     * Check if the user is the owner of the profile
     */
    boolean isOwner(Long userId, String emailFromToken);
}
