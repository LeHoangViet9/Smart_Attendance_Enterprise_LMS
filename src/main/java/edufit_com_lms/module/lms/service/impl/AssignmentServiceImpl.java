package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.lms.dto.response.AssignmentResponse;
import edufit_com_lms.module.lms.dto.request.CreateAssignmentRequest;
import edufit_com_lms.module.lms.dto.request.UpdateAssignmentRequest;
import edufit_com_lms.module.lms.entity.Assignment;
import edufit_com_lms.module.lms.repository.AssignmentRepository;
import edufit_com_lms.module.lms.repository.SubmissionRepository;
import edufit_com_lms.module.lms.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public AssignmentResponse createAssignment(CreateAssignmentRequest request) {
        Assignment assignment = Assignment.builder()
                .classId(request.getClassId())
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .maxScore(request.getMaxScore() != null ? request.getMaxScore() : 10.0)
                .attachmentUrl(request.getAttachmentUrl())
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(UUID id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Assignment not found with ID: " + id));
        return mapToResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByClassId(UUID classId) {
        return assignmentRepository.findByClassId(classId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentResponse updateAssignment(UUID id, UpdateAssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Assignment not found with ID: " + id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getDueDate() != null) {
            assignment.setDueDate(request.getDueDate());
        }
        if (request.getMaxScore() != null) {
            assignment.setMaxScore(request.getMaxScore());
        }
        if (request.getAttachmentUrl() != null) {
            assignment.setAttachmentUrl(request.getAttachmentUrl());
        }

        Assignment updated = assignmentRepository.save(assignment);
        return mapToResponse(updated);
    }

    @Override
    public void deleteAssignment(UUID id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFound("Assignment not found with ID: " + id);
        }
        submissionRepository.deleteByAssignmentId(id);
        assignmentRepository.deleteById(id);
    }

    private AssignmentResponse mapToResponse(Assignment assignment) {
        boolean isExpired = LocalDateTime.now().isAfter(assignment.getDueDate());
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .classId(assignment.getClassId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .attachmentUrl(assignment.getAttachmentUrl())
                .createdAt(assignment.getCreatedAt())
                .isExpired(isExpired)
                .build();
    }
}
