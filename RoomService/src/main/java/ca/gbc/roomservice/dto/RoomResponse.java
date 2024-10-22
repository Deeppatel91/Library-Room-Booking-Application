package ca.gbc.roomservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Long id;            // The unique identifier for the room
    private String roomName;    // The name of the room
    private Integer capacity;   // The capacity of the room
    private String features;    // Comma-separated string of features
    private Boolean available;  // Availability status of the room
}
