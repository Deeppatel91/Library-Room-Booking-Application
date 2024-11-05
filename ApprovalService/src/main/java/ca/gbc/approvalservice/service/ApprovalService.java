package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;

import java.util.List;

public interface ApprovalService {

    ApprovalResponse approveEvent(ApprovalRequest request, String token);

    ApprovalResponse getApprovalById(String id);

    List<ApprovalResponse> getAllApprovals();

    void deleteApproval(String id);

    List<ApprovalResponse> getApprovalsForEvent(String eventId);
}
