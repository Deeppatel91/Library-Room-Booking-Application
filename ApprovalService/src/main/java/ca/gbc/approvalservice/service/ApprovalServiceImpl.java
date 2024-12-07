package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.dto.Event;
import ca.gbc.approvalservice.dto.User;
import ca.gbc.approvalservice.Client.EventServiceFeignClient;
import ca.gbc.approvalservice.Client.UserServiceFeignClient;
import ca.gbc.approvalservice.model.Approval;
import ca.gbc.approvalservice.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final EventServiceFeignClient eventServiceFeignClient;
    private final UserServiceFeignClient userServiceFeignClient;

    @Override
    public ApprovalResponse approveEvent(ApprovalRequest request, String authorization) {
        log.info("Approving event with authorization token");

        // Validate that the approver is a STAFF member
        validateUserIsStaff(authorization, request.approverId());

        // Validate that the event exists
        validateEvent(authorization, request.eventId());

        // Save the approval details
        Approval approval = new Approval(
                null, // Assuming ID is auto-generated
                request.eventId(),
                request.approverId(),
                request.status(),
                request.comment(),
                LocalDateTime.now()
        );

        Approval savedApproval = approvalRepository.save(approval);
        log.info("Approval created successfully with ID: {}", savedApproval.getId());

        return mapToResponse(savedApproval);
    }

    @Override
    public ApprovalResponse getApprovalById(String id) {
        log.info("Fetching approval with ID: {}", id);

        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval not found"));

        return mapToResponse(approval);
    }

    @Override
    public List<ApprovalResponse> getAllApprovals() {
        log.info("Fetching all approvals");

        return approvalRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalResponse updateApproval(String id, ApprovalRequest request, String authorization) {
        log.info("Updating approval with ID: {}", id);

        // Fetch the existing approval
        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval not found"));

        // Validate that the approver is a STAFF member
        validateUserIsStaff(authorization, request.approverId());

        // Update the approval details
        approval.setStatus(request.status());
        approval.setComment(request.comment());

        Approval updatedApproval = approvalRepository.save(approval);
        log.info("Approval with ID: {} updated successfully", updatedApproval.getId());

        return mapToResponse(updatedApproval);
    }

    @Override
    public void deleteApproval(String id) {
        log.info("Deleting approval with ID: {}", id);

        // Fetch the existing approval
        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval not found"));

        approvalRepository.deleteById(id);
        log.info("Approval with ID: {} deleted successfully", id);
    }

    private void validateUserIsStaff(String authorization, String userId) {
        try {
            User user = userServiceFeignClient.getUserById(authorization, userId);
            if (user == null) {
                log.error("User ID: {} not found or service unavailable", userId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID: " + userId + " does not exist");
            }
            if (!"STAFF".equalsIgnoreCase(user.role())) {
                log.error("User ID: {} does not have the required STAFF role", userId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User with ID: " + userId + " does not have the STAFF role");
            }
            log.info("User ID: {} validated successfully with role: {}", userId, user.role());
        } catch (ResponseStatusException e) {
            log.error("User validation failed for User ID: {}, Reason: {}", userId, e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("General error in user validation for User ID: {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error during user validation");
        }
    }


    private void validateEvent(String authorization, String eventId) {
        try {
            Event event = eventServiceFeignClient.getEventById(authorization, eventId);
            if (event == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID: " + eventId + " not found");
            }
            log.info("Event ID: {} validated successfully", eventId);
        } catch (Exception e) {
            log.error("Event validation failed for Event ID: {}", eventId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Event ID: " + eventId, e);
        }
    }

    private ApprovalResponse mapToResponse(Approval approval) {
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
