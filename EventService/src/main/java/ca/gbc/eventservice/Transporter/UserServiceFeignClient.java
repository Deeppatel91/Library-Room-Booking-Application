package ca.gbc.eventservice.Transporter;

import ca.gbc.eventservice.dto.Users;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

// UserServiceFeignClient
@FeignClient(name = "UserService", url = "${USER_SERVICE_URL}")  // Ensure USER_SERVICE_URL is correctly configured
public interface UserServiceFeignClient {

    // Retrieves a user by ID with JWT token in Authorization header
    @GetMapping("/api/users/{userId}")
    Users getUserById(
            @PathVariable("userId") String id,
            @RequestHeader("Authorization") String token  // Pass JWT token here for secured communication
    );
}
