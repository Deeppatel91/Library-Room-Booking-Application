package ca.gbc.bookingservice.Transporter;

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
            // Call the RoomService API to check availability
            String response = roomServiceFeignClient.getRoomById(roomId);
            // Process response to check availability (assuming a simple response here)
            return response != null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check room availability", e);
        }
    }
}
