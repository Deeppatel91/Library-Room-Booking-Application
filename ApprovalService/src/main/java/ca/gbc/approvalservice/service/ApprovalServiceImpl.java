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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
        String formattedToken = formatBearerToken(token);

        Event event;
        try {
            event = eventServiceFeignClient.getEventById(request.eventId(), formattedToken);
        } catch (Exception e) {
            log.error("Failed to fetch event with ID {}: {}", request.eventId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to retrieve event with ID: " + request.eventId());
        }

        User approver;
        try {
            approver = userServiceFeignClient.getUserById(request.approverId(), formattedToken);
            if (approver == null || !"STAFF".equalsIgnoreCase(approver.role())) {
                log.error("User with ID {} does not have approval permissions", request.approverId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Approver does not have permission to approve events");
            }
        } catch (Exception e) {
            log.error("Failed to fetch user with ID {}: {}", request.approverId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Approver validation failed.");
        }

        Approval approval = new Approval();
        approval.setEventId(request.eventId());
        approval.setApproverId(request.approverId());
        approval.setStatus(request.status());
        approval.setComment(request.comment());
        approval.setApprovedAt(LocalDateTime.now());

        try {
            approval = approvalRepository.save(approval);
            log.info("Event with ID {} approved successfully by user {}", request.eventId(), request.approverId());
        } catch (Exception e) {
            log.error("Failed to save approval: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save approval.");
        }

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
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval not found");
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
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only STAFF users are allowed to update approvals.");
            }
        } catch (Exception e) {
            log.error("Error validating approver ID {}: {}", request.approverId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Approver validation failed: " + e.getMessage());
        }

        Approval existingApproval = approvalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval not found"));

        existingApproval.setStatus(request.status());
        existingApproval.setComment(request.comment());
        existingApproval.setApprovedAt(LocalDateTime.now());

        try {
            return mapToResponseDTO(approvalRepository.save(existingApproval));
        } catch (Exception e) {
            log.error("Error saving updated approval: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save updated approval");
        }
    }

    @Override
    public List<ApprovalResponse> getAllApprovals() {
        log.info("Fetching all approvals");
        try {
            return approvalRepository.findAll().stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all approvals: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve approvals");
        }
    }

    @Override
    public void deleteApproval(String id) {
        log.info("Deleting approval with ID: {}", id);
        if (!approvalRepository.existsById(id)) {
            log.error("Approval with ID {} not found for deletion", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval not found");
        }
        try {
            approvalRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete approval with ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete approval");
        }
    }

    @Override
    public List<ApprovalResponse> getApprovalsForEvent(String eventId) {
        log.info("Fetching approvals for event ID: {}", eventId);
        try {
            return approvalRepository.findByEventId(eventId).stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching approvals for event ID {}: {}", eventId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve approvals for event");
        }
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
