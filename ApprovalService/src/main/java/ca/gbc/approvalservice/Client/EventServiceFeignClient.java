package ca.gbc.approvalservice.Client;

import ca.gbc.approvalservice.dto.Event;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "EventService", url = "${event.service.url}")
public interface EventServiceFeignClient {

    @GetMapping("/api/events/{id}")
    Event getEventById(
            @PathVariable("id") String eventId,
            @RequestHeader("Authorization") String token
    );
}
