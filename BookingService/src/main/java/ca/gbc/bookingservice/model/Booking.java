package ca.gbc.bookingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Bookings")
public class Booking {

    @Id
    private String id;
    private String userId;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;

}
