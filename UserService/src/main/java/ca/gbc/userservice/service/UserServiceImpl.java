package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.AuthorizationRequest;
import ca.gbc.userservice.dto.AuthorizationResponse;
import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.model.Users;
import ca.gbc.userservice.repository.UsersRepository;
import ca.gbc.userservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder; // For encoding passwords
    private final JwtUtil jwtUtil; // Utility class for JWT token operations

    @Autowired
    public UserServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user
     */
    public UserResponse registerUser(UserRequest userRequest) {
        // Check if the user with the same email already exists
        if (isEmailTaken(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Encrypt the password before saving it
        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());

        // Create and save new user
        Users newUser = saveNewUser(userRequest, encodedPassword);

        // Generate JWT token for the new user
        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getRole());

        // Return user response with token
        return mapToUserResponse(newUser, token);
    }

    /**
     * Authenticate the user and generate a JWT token
     */
    public AuthorizationResponse authenticateUser(AuthorizationRequest authorizationRequest) {
        Users user = usersRepository.findByEmail(authorizationRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the provided password matches the stored encrypted password
        if (!passwordEncoder.matches(authorizationRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Return the token and user details
        return mapToAuthorizationResponse(user, token);
    }

    // Utility method to check if email is already taken
    private boolean isEmailTaken(String email) {
        return usersRepository.findByEmail(email).isPresent();
    }

    // Save new user to the repository
    private Users saveNewUser(UserRequest userRequest, String encodedPassword) {
        Users newUser = Users.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .password(encodedPassword)
                .role(userRequest.getRole())
                .userType(userRequest.getUserType())
                .build();

        return usersRepository.save(newUser);
    }

    // Utility method to map user to UserResponse
    private UserResponse mapToUserResponse(Users user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .userType(user.getUserType())
                .token(token)
                .build();
    }

    // Utility method to map user to AuthorizationResponse
    private AuthorizationResponse mapToAuthorizationResponse(Users user, String token) {
        return AuthorizationResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .userType(user.getUserType())
                .build();
    }
}
