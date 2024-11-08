package ca.gbc.approvalservice.dto;

public record ApprovalRequest(
        String eventId,
        String approverId,
        String comment,
        String status
) {
}
