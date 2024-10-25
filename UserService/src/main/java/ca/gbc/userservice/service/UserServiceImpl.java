package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.AuthorizationRequest;
import ca.gbc.userservice.dto.AuthorizationResponse;
import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.model.Users;
import ca.gbc.userservice.repository.UsersRepository;
import ca.gbc.userservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        if (isEmailTaken(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        Users newUser = saveNewUser(userRequest, encodedPassword);
        String token = jwtTokenProvider.generateToken(newUser.getEmail(), newUser.getRole());

        return mapToUserResponse(newUser, token);
    }

    @Override
    public AuthorizationResponse authenticateUser(AuthorizationRequest authorizationRequest) {
        Users user = usersRepository.findByEmail(authorizationRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(authorizationRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
        return mapToAuthorizationResponse(user, token);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest, String token) {
        Users currentUser = validateUserOrAdmin(id, token);

        currentUser.setName(userRequest.getName());
        currentUser.setEmail(userRequest.getEmail());
        currentUser.setRole(userRequest.getRole());
        currentUser.setUserType(userRequest.getUserType());

        usersRepository.save(currentUser);

        String updatedToken = jwtTokenProvider.generateToken(currentUser.getEmail(), currentUser.getRole());
        return mapToUserResponse(currentUser, updatedToken);
    }

    @Override
    public void deleteUser(Long id, String token) {
        Users currentUser = validateUserOrAdmin(id, token);
        usersRepository.deleteById(currentUser.getId());
    }

    @Override
    public boolean isOwner(Long userId, String emailFromToken) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getEmail().equals(emailFromToken);
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

    // Check if the user is authorized to update/delete their own profile or if they are an admin
    private Users validateUserOrAdmin(Long id, String token) {
        String emailFromToken = jwtTokenProvider.getUsername(token);
        String roleFromToken = jwtTokenProvider.getRole(token);

        // Get the user by ID
        Users userToUpdate = usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Allow if the current user is the owner of the profile or an admin
        if (userToUpdate.getEmail().equals(emailFromToken) || roleFromToken.equals("ADMIN")) {
            return userToUpdate;
        } else {
            throw new SecurityException("You are not authorized to perform this action");
        }
    }


}
