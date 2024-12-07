package ca.gbc.approvalservice.Client;

import ca.gbc.approvalservice.dto.User;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceFeignClient {
    @GetMapping("/api/users/{id}")
    @CircuitBreaker(name = "userServiceCircuitBreaker", fallbackMethod = "fallbackUser")
    User getUserById(@RequestHeader("Authorization") String authorization, @PathVariable("id") String userId);

    default User fallbackUser(String authorization, String userId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID: " + userId + " does not exist");
        } else {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "UserService is unavailable for the approval Service ");
        }
    }

}

