package ca.gbc.bookingservice.Client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service", url = "${ROOM_SERVICE_URL}", fallback = RoomServiceClientFallback.class)
public interface RoomServiceFeignClient {

    @GetMapping("/api/rooms/{id}")
    @CircuitBreaker(name = "roomServiceCircuitBreaker", fallbackMethod = "roomServiceFallback")
    String getRoomById(@PathVariable("id") Long id);
}

class RoomServiceClientFallback implements RoomServiceFeignClient {
    @Override
    public String getRoomById(Long id) {
        return "Service Unavailable due to RoomService Is down";
    }
}

































