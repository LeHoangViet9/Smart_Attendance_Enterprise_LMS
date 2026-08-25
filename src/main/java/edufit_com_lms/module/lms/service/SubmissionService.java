package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.request.GradeSubmissionRequest;
import edufit_com_lms.module.lms.dto.request.SubmitAssignmentRequest;
import edufit_com_lms.module.lms.dto.response.SubmissionResponse;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {
    SubmissionResponse submitAssignment(UUID assignmentId, SubmitAssignmentRequest request);
    SubmissionResponse gradeSubmission(UUID submissionId, GradeSubmissionRequest request);
    SubmissionResponse getSubmissionById(UUID id);
    SubmissionResponse getSubmissionByAssignmentAndStudent(UUID assignmentId, UUID studentId);
    List<SubmissionResponse> getSubmissionsByAssignment(UUID assignmentId);
}
