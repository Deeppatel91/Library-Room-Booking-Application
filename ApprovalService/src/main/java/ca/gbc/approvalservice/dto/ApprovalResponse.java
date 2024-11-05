package ca.gbc.approvalservice.dto;

import java.time.LocalDateTime;

public record ApprovalResponse(
        String id,
        String eventId,
        String approverId,
        String status,  // "APPROVED" or "REJECTED"
        String comment,
        LocalDateTime approvedAt
) {
}
