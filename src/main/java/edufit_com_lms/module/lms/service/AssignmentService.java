package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.response.AssignmentResponse;
import edufit_com_lms.module.lms.dto.request.CreateAssignmentRequest;
import edufit_com_lms.module.lms.dto.request.UpdateAssignmentRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface AssignmentService {
    AssignmentResponse createAssignment(CreateAssignmentRequest request, Long lecturerId);

    AssignmentResponse getAssignmentById(UUID id);

    List<AssignmentResponse> getAllAssignments();

    Page<AssignmentResponse> getPaginatedAssignments(Pageable pageable);

    List<AssignmentResponse> getAssignmentsByClassId(UUID classId);

    Page<AssignmentResponse> getPaginatedAssignmentsByClassId(UUID classId, Pageable pageable);

    Page<AssignmentResponse> getPaginatedAssignmentsByClassIdIn(List<UUID> classIds, Pageable pageable);

    AssignmentResponse updateAssignment(UUID id, UpdateAssignmentRequest request, Long lecturerId);

    void deleteAssignment(UUID id, Long lecturerId);
}
