package ca.gbc.bookingservice.Transporter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service", url = "${ROOM_SERVICE_URL}")
public interface RoomServiceFeignClient {
    @GetMapping("/api/rooms/{roomId}")
    String getRoomById(@PathVariable("roomId") String roomId);
}

