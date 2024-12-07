package ca.gbc.approvalservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "approvals")
public class Approval {
    @Id
    private String id;
    private String eventId;
    private String approverId;
    private String status;
    private String comment;
    private LocalDateTime approvedAt;
}