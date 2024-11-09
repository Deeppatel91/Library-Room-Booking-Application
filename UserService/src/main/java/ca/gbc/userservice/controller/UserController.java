package ca.gbc.userservice.controller;

import ca.gbc.userservice.dto.AuthorizationRequest;
import ca.gbc.userservice.dto.AuthorizationResponse;
import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.model.Roles;
import ca.gbc.userservice.model.Users;
import ca.gbc.userservice.repository.UsersRepository;
import ca.gbc.userservice.SecurityConfigurations.JwtTokenProvider;
import ca.gbc.userservice.service.UserService;
import ca.gbc.userservice.service.UsersInformationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsersRepository usersRepository;

    @Autowired
    private UsersInformationImpl userDetailsService;

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

    @PostMapping("/init")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        try {
            if (userService.noAdminExists() || request.getRole() == Roles.ADMIN) {
                UserResponse userResponse = userService.createUser(request);
                return ResponseEntity.ok(userResponse);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid role or userType: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createNewUser(@RequestBody UserRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        Optional<Users> optionalUser = usersRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        UserDetails userDetails = userService.loadUserByUsername(optionalUser.get().getEmail());
        if (!jwtTokenProvider.validateToken(token, userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }



    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
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

    @PutMapping("/Deactivate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long userId) {
        var userResponse = userService.deactivateUser(userId);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/Activate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long userId) {
        var userResponse = userService.activateUser(userId);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/login")
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AuthorizationResponse("Authentication failed"));
        }
    }

    @PutMapping("/Role/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable Long userId, @RequestParam String role) {
        try {
            Roles roleEnum = Roles.valueOf(role.toUpperCase());
            var userResponse = userService.changeUserRole(userId, roleEnum);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UserResponse());
        }
    }


}