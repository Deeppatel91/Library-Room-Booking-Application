package ca.gbc.approvalservice.controller;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Slf4j
public class ApprovalController {
    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<?> approveEvent(@RequestBody ApprovalRequest request, @RequestHeader("Authorization") String token) {
        try {
            ApprovalResponse approval = approvalService.approveEvent(request, token);
            return ResponseEntity.ok(approval);
        } catch (ResponseStatusException e) {
            log.error("Error approving event: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("Unexpected error approving event: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error approving event");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllApprovals() {
        try {
            List<ApprovalResponse> approvals = approvalService.getAllApprovals();
            return ResponseEntity.ok(approvals);
        } catch (ResponseStatusException e) {
            log.error("Error retrieving approvals: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("Unexpected error retrieving approvals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving approvals");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApprovalById(@PathVariable String id) {
        try {
            ApprovalResponse approval = approvalService.getApprovalById(id);
            return ResponseEntity.ok(approval);
        } catch (ResponseStatusException e) {
            log.error("Error retrieving approval with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("Unexpected error retrieving approval with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving approval");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateApproval(@PathVariable String id, @RequestBody ApprovalRequest request, @RequestHeader("Authorization") String token) {
        try {
            ApprovalResponse updatedApproval = approvalService.updateApproval(id, request, token);
            return ResponseEntity.ok(updatedApproval);
        } catch (ResponseStatusException e) {
            log.error("Error updating approval with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("Unexpected error updating approval with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error updating approval");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApproval(@PathVariable String id) {
        try {
            approvalService.deleteApproval(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            log.error("Error deleting approval with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("Unexpected error deleting approval with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error deleting approval");
        }
    }
}
