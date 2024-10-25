package ca.gbc.userservice.controller;

import ca.gbc.userservice.dto.AuthorizationRequest;
import ca.gbc.userservice.dto.AuthorizationResponse;
import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.security.JwtTokenProvider;
import ca.gbc.userservice.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.registerUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Authenticate user (login) and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthorizationResponse> authenticateUser(@RequestBody AuthorizationRequest authorizationRequest) {
        AuthorizationResponse authResponse = userService.authenticateUser(authorizationRequest);
        return ResponseEntity.ok(authResponse);
    }


    /**
     * Update a user's profile (either their own or admin can update any user)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        String emailFromToken = jwtTokenProvider.getUsername(token);
        String roleFromToken = jwtTokenProvider.getRole(token);

        // Only allow update if the user is the owner of the profile or an admin
        if (userService.isOwner(id, emailFromToken) || "ADMIN".equals(roleFromToken)) {
            UserResponse updatedUser = userService.updateUser(id, userRequest, token);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.status(403).body(null); // Forbidden
        }
    }

    /**
     * Delete a user's profile (either their own or admin can delete any user)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        String emailFromToken = jwtTokenProvider.getUsername(token);
        String roleFromToken = jwtTokenProvider.getRole(token);

        // Only allow deletion if the user is the owner of the profile or an admin
        if (userService.isOwner(id, emailFromToken) || "ADMIN".equals(roleFromToken)) {
            userService.deleteUser(id, token);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(403).body("You are not authorized to delete this profile");
        }
    }
}
