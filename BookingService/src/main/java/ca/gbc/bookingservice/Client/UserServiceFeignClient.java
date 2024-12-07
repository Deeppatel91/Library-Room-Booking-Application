package ca.gbc.bookingservice.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "user-service", url = "${USER_SERVICE_URL}")
public interface UserServiceFeignClient {

    @GetMapping("/api/users/{userId}")
    String getUserById(@RequestHeader("Authorization") String token, @PathVariable("userId") Long userId);
}
