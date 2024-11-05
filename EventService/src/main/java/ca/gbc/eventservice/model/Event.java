package ca.gbc.eventservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "events")
@AllArgsConstructor
public class Event {
    @Id
    private String id;
    private String organizerId;
    private String eventName;
    private String eventType;
    private String bookingId;
    private int expectedAttendees;

}
