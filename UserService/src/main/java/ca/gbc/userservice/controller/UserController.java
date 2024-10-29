package ca.gbc.userservice.controller;

import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.dto.AuthorizationRequest;
import ca.gbc.userservice.dto.AuthorizationResponse;
import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.model.Users;
import ca.gbc.userservice.repository.UsersRepository;
import ca.gbc.userservice.security.JwtTokenProvider;
import ca.gbc.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsersRepository usersRepository;

    @Autowired
    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UsersRepository usersRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usersRepository = usersRepository;
    }

    // User Management Endpoints
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        var userResponse = userService.createUser(request);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{userId}")
    // Uncomment for access control: @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'FACULTY', 'STUDENT')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        var userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'FACULTY')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @RequestBody UserRequest request) {
        var updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // Admin Endpoints
    @PutMapping("/deactivate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long userId) {
        var userResponse = userService.deactivateUser(userId);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/activate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long userId) {
        var userResponse = userService.activateUser(userId);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/role/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable Long userId, @RequestParam String role) {
        var userResponse = userService.changeUserRole(userId, role);
        return ResponseEntity.ok(userResponse);
    }

    // Authentication Endpoint
    @PostMapping("/authenticate")
    public ResponseEntity<AuthorizationResponse> createAuthenticationToken(@RequestBody AuthorizationRequest request) {
        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            final UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
            Users user = usersRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));


            final String jwt = jwtTokenProvider.generateToken(userDetails, String.valueOf(user.getId()));

            return ResponseEntity.ok(new AuthorizationResponse(jwt));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(403).body(new AuthorizationResponse("Authentication failed"));
        }
    }
}
