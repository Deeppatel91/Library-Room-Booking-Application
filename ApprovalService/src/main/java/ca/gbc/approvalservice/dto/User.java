package ca.gbc.approvalservice.dto;

public record User(
        String id,

        String email,
        String role  // For example, "STAFF", "ADMIN", etc.
) {
}
