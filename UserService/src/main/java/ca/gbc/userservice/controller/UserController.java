package ca.gbc.userservice.controller;

import ca.gbc.userservice.dto.AuthorizationRequest;
import ca.gbc.userservice.dto.AuthorizationResponse;
import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    /**
     * Register a new user
     * @param userRequest user details for registration
     * @return UserResponse with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.registerUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Authenticate user (login) and return JWT token
     * @param authorizationRequest user credentials (email and password)
     * @return AuthorizationResponse with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthorizationResponse> authenticateUser(@RequestBody AuthorizationRequest authorizationRequest) {
        AuthorizationResponse authResponse = userService.authenticateUser(authorizationRequest);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Example of a secured endpoint that requires a valid JWT token to access
     * @return String message if token is valid
     */
    @GetMapping("/secure")
    public ResponseEntity<String> securedEndpoint() {
        // This endpoint could be secured by a JWT token validation filter (configured separately in Spring Security)
        return ResponseEntity.ok("You have accessed a secured endpoint!");
    }
}
