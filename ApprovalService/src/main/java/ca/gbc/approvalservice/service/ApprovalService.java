package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;

import java.util.List;

public interface ApprovalService {

    /**
     * Approve an event.
     *
     * @param request      the approval request
     * @param authorization the authorization token
     * @return the saved approval response
     */
    ApprovalResponse approveEvent(ApprovalRequest request, String authorization);

    /**
     * Retrieve an approval by its ID.
     *
     * @param id the approval ID
     * @return the approval response
     */
    ApprovalResponse getApprovalById(String id);

    /**
     * Retrieve all approvals.
     *
     * @return a list of all approval responses
     */
    List<ApprovalResponse> getAllApprovals();

    /**
     * Update an existing approval.
     *
     * @param id           the approval ID
     * @param request      the updated approval request
     * @param authorization the authorization token
     * @return the updated approval response
     */
    ApprovalResponse updateApproval(String id, ApprovalRequest request, String authorization);

    /**
     * Delete an approval by its ID.
     *
     * @param id the approval ID
     */
    void deleteApproval(String id);
}
