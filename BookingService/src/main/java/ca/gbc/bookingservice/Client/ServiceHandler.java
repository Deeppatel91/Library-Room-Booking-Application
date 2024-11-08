package ca.gbc.bookingservice.Client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceHandler {
    private final RoomServiceFeignClient roomServiceFeignClient;

    @Autowired
    public ServiceHandler(RoomServiceFeignClient roomServiceFeignClient) {
        this.roomServiceFeignClient = roomServiceFeignClient;
    }

    public boolean isRoomAvailable(String roomId) {
        try {
            String response = roomServiceFeignClient.getRoomById(roomId);
            return response != null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check room availability for BookingService", e);
        }
    }
}
