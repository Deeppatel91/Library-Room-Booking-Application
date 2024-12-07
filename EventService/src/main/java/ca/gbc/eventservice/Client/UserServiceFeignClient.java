package ca.gbc.eventservice.Client;

import ca.gbc.eventservice.dto.Users;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${USER_SERVICE_URL}")
public interface UserServiceFeignClient {
    @GetMapping("/api/users/{id}")
    Users getUserById(@RequestHeader("Authorization") String authorization, @PathVariable("id") String userId);
}
