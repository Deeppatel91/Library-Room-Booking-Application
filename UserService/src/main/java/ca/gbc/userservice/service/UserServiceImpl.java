package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.model.Users;
import ca.gbc.userservice.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRequest request) {
        try {
            Users user = Users.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .userType(request.getUserType())
                    .active(true)
                    .build();

            Users savedUser = usersRepository.save(user);
            return mapToResponse(savedUser);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create user", e);
        }
    }

    @Override
    public UserResponse getUserById(Long userId) {
        try {
            Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for ID: " + userId));
            return mapToResponse(user);
        } catch (Exception e) {
            log.error("Error fetching user by ID {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public UserResponse updateUser(Long userId, UserRequest request) {
        try {
            Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for ID: " + userId));

            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole());
            user.setUserType(request.getUserType());

            Users updatedUser = usersRepository.save(user);
            return mapToResponse(updatedUser);
        } catch (Exception e) {
            log.error("Error updating user with ID {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update user", e);
        }
    }

    @Override
    public void deleteUser(Long userId) {
        try {
            if (!usersRepository.existsById(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for ID: " + userId);
            }
            usersRepository.deleteById(userId);
        } catch (Exception e) {
            log.error("Error deleting user with ID {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete user", e);
        }
    }

    @Override
    public UserResponse deactivateUser(Long userId) {
        try {
            Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for ID: " + userId));

            user.setActive(false);
            Users deactivatedUser = usersRepository.save(user);
            return mapToResponse(deactivatedUser);
        } catch (Exception e) {
            log.error("Error deactivating user with ID {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to deactivate user", e);
        }
    }

    @Override
    public UserResponse activateUser(Long userId) {
        try {
            Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for ID: " + userId));

            user.setActive(true);
            Users activatedUser = usersRepository.save(user);
            return mapToResponse(activatedUser);
        } catch (Exception e) {
            log.error("Error activating user with ID {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to activate user", e);
        }
    }

    @Override
    public UserResponse logIn(String email, String password) {
        try {
            Users user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }

            return mapToResponse(user);
        } catch (Exception e) {
            log.error("Error during login for email {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<UserResponse> getAllUsers() {
        try {
            return usersRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to fetch users", e);
        }
    }

    private UserResponse mapToResponse(Users user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .userType(user.getUserType())
                .active(user.isActive())
                .build();
    }
}
