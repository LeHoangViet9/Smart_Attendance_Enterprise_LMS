package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.response.AssignmentResponse;
import edufit_com_lms.module.lms.dto.request.CreateAssignmentRequest;
import edufit_com_lms.module.lms.dto.request.UpdateAssignmentRequest;

import java.util.List;
import java.util.UUID;

public interface AssignmentService {
    AssignmentResponse createAssignment(CreateAssignmentRequest request);
    AssignmentResponse getAssignmentById(UUID id);
    List<AssignmentResponse> getAssignmentsByClassId(UUID classId);
    AssignmentResponse updateAssignment(UUID id, UpdateAssignmentRequest request);
    void deleteAssignment(UUID id);
}
