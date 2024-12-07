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
    public ResponseEntity<ApprovalResponse> approveEvent(@RequestHeader("Authorization") String authorization, @RequestBody ApprovalRequest request) {
        try {
            log.info("Request to approve event: {}", request);
            ApprovalResponse approval = approvalService.approveEvent(request, authorization);
            return ResponseEntity.ok(approval);
        } catch (ResponseStatusException e) {
            log.error("Error during approval process: {}, Status: {}", e.getReason(), e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Unexpected error during approval process: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApprovalResponse> getApprovalById(@PathVariable String id) {
        try {
            log.info("Fetching approval by ID: {}", id);
            ApprovalResponse approval = approvalService.getApprovalById(id);
            return ResponseEntity.ok(approval);
        } catch (Exception e) {
            log.error("Error fetching approval by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ApprovalResponse>> getAllApprovals() {
        try {
            log.info("Fetching all approvals");
            List<ApprovalResponse> approvals = approvalService.getAllApprovals();
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            log.error("Error fetching all approvals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalResponse> updateApproval(@PathVariable String id, @RequestHeader("Authorization") String authorization, @RequestBody ApprovalRequest request) {
        try {
            log.info("Request to update approval with ID: {}", id);
            ApprovalResponse updatedApproval = approvalService.updateApproval(id, request, authorization);
            return ResponseEntity.ok(updatedApproval);
        } catch (Exception e) {
            log.error("Error updating approval with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApproval(@PathVariable String id, @RequestHeader("Authorization") String authorization) {
        try {
            log.info("Request to delete approval with ID: {}", id);
            approvalService.deleteApproval(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting approval with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
