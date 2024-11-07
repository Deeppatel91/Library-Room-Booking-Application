
package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.dto.Event;
import ca.gbc.approvalservice.dto.User;
import ca.gbc.approvalservice.model.Approval;
import ca.gbc.approvalservice.repository.ApprovalRepository;
import ca.gbc.approvalservice.Client.EventServiceFeignClient;
import ca.gbc.approvalservice.Client.UserServiceFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final EventServiceFeignClient eventServiceFeignClient;
    private final UserServiceFeignClient userServiceFeignClient;

    @Override
    public ApprovalResponse approveEvent(ApprovalRequest request, String token) {
        log.info("Approving event with ID: {}", request.eventId());

        // Ensure token is trimmed and prefixed correctly
        String formattedToken = formatBearerToken(token);

        Event event;
        try {
            event = eventServiceFeignClient.getEventById(request.eventId(), formattedToken);
        } catch (Exception e) {
            log.error("Failed to fetch event with ID {}: {}", request.eventId(), e.getMessage());
            throw new IllegalArgumentException("Failed to retrieve event with ID: " + request.eventId());
        }

        if (event == null) {
            log.error("Event with ID {} not found", request.eventId());
            throw new IllegalArgumentException("Invalid event ID");
        }

        User approver;
        try {
            approver = userServiceFeignClient.getUserById(request.approverId(), formattedToken);
        } catch (Exception e) {
            log.error("Failed to fetch user with ID {}: {}", request.approverId(), e.getMessage());
            throw new SecurityException("Approver validation failed.");
        }

        if (approver == null || !"STAFF".equalsIgnoreCase(approver.role())) {
            log.error("User with ID {} does not have approval permissions", request.approverId());
            throw new SecurityException("Approver does not have permission to approve events");
        }

        Approval approval = new Approval();
        approval.setEventId(request.eventId());
        approval.setApproverId(request.approverId());
        approval.setStatus(request.status());
        approval.setComment(request.comment());
        approval.setApprovedAt(LocalDateTime.now());

        approval = approvalRepository.save(approval);
        log.info("Event with ID {} approved successfully by user {}", request.eventId(), request.approverId());

        return mapToResponseDTO(approval);
    }

    private String formatBearerToken(String token) {
        return token != null && !token.trim().startsWith("Bearer ") ? "Bearer " + token.trim() : token;
    }
    @Override
    public ApprovalResponse getApprovalById(String id) {
        log.info("Fetching approval with ID: {}", id);
        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Approval with ID {} not found", id);
                    return new IllegalArgumentException("Approval not found");
                });
        return mapToResponseDTO(approval);
    }
    @Override
    public ApprovalResponse updateApproval(String id, ApprovalRequest request, String token) {
        log.info("Updating approval with ID: {}", id);

        String formattedToken = formatBearerToken(token);

        User approver;
        try {
            approver = userServiceFeignClient.getUserById(request.approverId(), formattedToken);
            if (approver == null || !"STAFF".equalsIgnoreCase(approver.role())) {
                log.error("User with ID {} does not have permission to update approvals", request.approverId());
                throw new SecurityException("Only STAFF users are allowed to update approvals.");
            }
        } catch (Exception e) {
            log.error("Error validating approver ID {}: {}", request.approverId(), e.getMessage());
            throw new SecurityException("Approver validation failed: " + e.getMessage());
        }

        Approval existingApproval;
        try {
            existingApproval = approvalRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Approval not found"));
        } catch (Exception e) {
            log.error("Error fetching approval with ID {}: {}", id, e.getMessage());
            throw new IllegalArgumentException("Error finding approval with ID: " + id);
        }

        // Update approval fields
        existingApproval.setStatus(request.status());
        existingApproval.setComment(request.comment());
        existingApproval.setApprovedAt(LocalDateTime.now());

        // Save the updated approval
        Approval updatedApproval;
        try {
            updatedApproval = approvalRepository.save(existingApproval);
        } catch (Exception e) {
            log.error("Error saving updated approval: {}", e.getMessage());
            throw new RuntimeException("Failed to save updated approval");
        }

        log.info("Approval with ID {} updated successfully by staff {}", id, request.approverId());
        return mapToResponseDTO(updatedApproval);
    }


    @Override
    public List<ApprovalResponse> getAllApprovals() {
        log.info("Fetching all approvals");
        return approvalRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteApproval(String id) {
        log.info("Deleting approval with ID: {}", id);
        if (!approvalRepository.existsById(id)) {
            log.error("Approval with ID {} not found for deletion", id);
            throw new IllegalArgumentException("Approval not found");
        }
        approvalRepository.deleteById(id);
    }

    @Override
    public List<ApprovalResponse> getApprovalsForEvent(String eventId) {
        log.info("Fetching approvals for event ID: {}", eventId);
        return approvalRepository.findByEventId(eventId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private ApprovalResponse mapToResponseDTO(Approval approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getEventId(),
                approval.getApproverId(),
                approval.getStatus(),
                approval.getComment(),
                approval.getApprovedAt()
        );
    }
}
