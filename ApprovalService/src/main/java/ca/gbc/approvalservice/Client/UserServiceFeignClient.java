package ca.gbc.approvalservice.Client;

import ca.gbc.approvalservice.dto.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "UserService", url = "${user.service.url}")
public interface UserServiceFeignClient {

    @GetMapping("/api/users/{userId}")
    User getUserById(
            @PathVariable("userId") String id,
            @RequestHeader("Authorization") String token
    );
}
