package ca.gbc.approvalservice.dto;

public record ApprovalRequest(
        String eventId,
        String approverId,
        String status,  // "APPROVED" or "REJECTED"
        String comment
) {
}
