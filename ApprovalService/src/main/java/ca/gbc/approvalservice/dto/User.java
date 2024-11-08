package ca.gbc.approvalservice.dto;

public record User(
        String id,
        String role,
        String email
) {
}
