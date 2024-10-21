package ca.gbc.roomservice.dto;

public class RoomRequest {
    private String roomName;
    private Integer capacity;
    private String features;
    private Boolean available;

    // Default constructor for JSON serialization/deserialization
    public RoomRequest() {
    }

    // Constructor with all fields
    public RoomRequest(String roomName, Integer capacity, String features, Boolean available) {
        this.roomName = roomName;
        this.capacity = capacity;
        this.features = features;
        this.available = available;
    }

    // Getters and setters
    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    // ToString method for logging and debugging purposes
    @Override
    public String toString() {
        return "RoomRequest{" +
                "roomName='" + roomName + '\'' +
                ", capacity=" + capacity +
                ", features='" + features + '\'' +
                ", available=" + available +
                '}';
    }
}
