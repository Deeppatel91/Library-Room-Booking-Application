package ca.gbc.approvalservice.controller;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<ApprovalResponse> approveEvent(@RequestBody ApprovalRequest request, @RequestHeader("Authorization") String token) {
        ApprovalResponse approval = approvalService.approveEvent(request, token);
        return ResponseEntity.ok(approval);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalResponse> getApprovalById(@PathVariable String id) {
        return ResponseEntity.ok(approvalService.getApprovalById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ApprovalResponse>> getAllApprovals() {
        return ResponseEntity.ok(approvalService.getAllApprovals());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalResponse> updateApproval(@PathVariable String id, @RequestBody ApprovalRequest request, @RequestHeader("Authorization") String token) {
        ApprovalResponse updatedApproval = approvalService.updateApproval(id, request, token);
        return ResponseEntity.ok(updatedApproval);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApproval(@PathVariable String id) {
        approvalService.deleteApproval(id);
        return ResponseEntity.noContent().build();
    }
}