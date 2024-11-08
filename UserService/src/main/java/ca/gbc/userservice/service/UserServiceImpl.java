package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.model.Roles;
import ca.gbc.userservice.model.Users;
import ca.gbc.userservice.repository.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public UserResponse createUser(UserRequest request) {
        Users user = new Users();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setUserType(request.getUserType());
        user.setActive(true);
        Users savedUser = usersRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for the given id"));
        return mapToResponse(user);
    }

    @Override
    public UserResponse updateUser(Long userId, UserRequest request) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for the given id"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setUserType(request.getUserType());

        Users updatedUser = usersRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        usersRepository.deleteById(userId);
    }

    @Override
    public UserResponse deactivateUser(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for the given id"));
        user.setActive(false);
        Users updatedUser = usersRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse activateUser(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for the given id"));
        user.setActive(true);
        Users updatedUser = usersRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse changeUserRole(Long userId, Roles role) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for the given id"));
        user.setRole(role);
        Users updatedUser = usersRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    @Override
    public boolean noAdminExists() {
        return usersRepository.countByRole(Roles.ADMIN) == 0;
    }

    @Override
    public Object changeUserRole(Long userId, String name) {
        return null;
    }

    @Override
    public Roles getUserRole(String organizerId) {
        Users user = usersRepository.findById(Long.parseLong(organizerId))
                .orElseThrow(() -> new RuntimeException("User not found for the given id"));
        return user.getRole();
    }
    private UserResponse mapToResponse(Users user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getUserType(),
                user.isActive()
        );
    }
}
