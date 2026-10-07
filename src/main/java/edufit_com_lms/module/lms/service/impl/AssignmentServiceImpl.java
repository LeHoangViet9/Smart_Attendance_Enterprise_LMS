package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.lms.dto.request.CreateAssignmentRequest;
import edufit_com_lms.module.lms.dto.request.UpdateAssignmentRequest;
import edufit_com_lms.module.lms.dto.response.AssignmentResponse;
import edufit_com_lms.module.lms.entity.Assignment;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.AssignmentRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import edufit_com_lms.module.lms.repository.SubmissionRepository;
import edufit_com_lms.module.lms.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private final SchoolClassRepository schoolClassRepository;
    private final edufit_com_lms.module.lms.repository.ClassEnrollmentRepository classEnrollmentRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    private void validateLecturerOwnership(List<UUID> classIds, Long lecturerId) {
        if (lecturerId == null) return; // Admin skips validation
        for (UUID classId : classIds) {
            SchoolClass schoolClass = schoolClassRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFound("Class not found with ID: " + classId));
            if (schoolClass.getLecturer() == null || !schoolClass.getLecturer().getUserId().equals(lecturerId)) {
                throw new RuntimeException("Bạn không có quyền quản lý Assignment của lớp học này.");
            }
        }
    }

    @Override
    public AssignmentResponse createAssignment(CreateAssignmentRequest request, Long lecturerId) {
        validateLecturerOwnership(request.getClassIds(), lecturerId);
        Assignment assignment = Assignment.builder()
                .classes(schoolClassRepository.findAllById(request.getClassIds()))
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .maxScore(request.getMaxScore() != null ? request.getMaxScore() : 10.0)
                .attachmentUrl(request.getAttachmentUrl())
                .isExam(request.getIsExam() != null ? request.getIsExam() : false)
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        
        // Notify all enrolled students
        for (UUID classId : request.getClassIds()) {
            List<edufit_com_lms.module.lms.entity.ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(classId);
            for (edufit_com_lms.module.lms.entity.ClassEnrollment enrollment : enrollments) {
            eventPublisher.publishEvent(edufit_com_lms.module.notification.event.NotificationEvent.builder()
                    .title("BÃ i táº­p má»›i: " + saved.getTitle())
                    .message("Giáº£ng viÃªn vá»«a giao bÃ i táº­p má»›i. Háº¡n ná»™p: " + (saved.getDueDate() != null ? saved.getDueDate() : "KhÃ´ng cÃ³ háº¡n"))
                    .type("SYSTEM_LOG")
                    .recipientId(enrollment.getStudent().getUserId())
                    .build());
        }

                }

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
    public Page<AssignmentResponse> getPaginatedAssignments(Pageable pageable) {
        Page<Assignment> pageResult = assignmentRepository.findAll(pageable);
        List<AssignmentResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByClassId(UUID classId) {
        return assignmentRepository.findByClasses_Id(classId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getPaginatedAssignmentsByClassId(UUID classId, Pageable pageable) {
        Page<Assignment> pageResult = assignmentRepository.findByClasses_Id(classId, pageable);
        List<AssignmentResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getPaginatedAssignmentsByClassIdIn(List<UUID> classIds, Pageable pageable) {
        Page<Assignment> pageResult = assignmentRepository.findDistinctByClasses_IdIn(classIds, pageable);
        List<AssignmentResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    @Override
    public AssignmentResponse updateAssignment(UUID id, UpdateAssignmentRequest request, Long lecturerId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Assignment not found with ID: " + id));

        validateLecturerOwnership(assignment.getClasses().stream().map(SchoolClass::getId).collect(Collectors.toList()), lecturerId);

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
        if (request.getIsExam() != null) {
            assignment.setIsExam(request.getIsExam());
        }
        if (request.getIsPublished() != null) {
            assignment.setIsPublished(request.getIsPublished());
        }

        Assignment updated = assignmentRepository.save(assignment);
        return mapToResponse(updated);
    }

    @Override
    public void deleteAssignment(UUID id, Long lecturerId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Assignment not found with ID: " + id));

        validateLecturerOwnership(assignment.getClasses().stream().map(SchoolClass::getId).collect(Collectors.toList()), lecturerId);
        submissionRepository.deleteByAssignmentId(id);
        assignmentRepository.deleteById(id);
    }

    private AssignmentResponse mapToResponse(Assignment assignment) {
        boolean isExpired = assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate());
        List<String> classNames = assignment.getClasses().stream().map(SchoolClass::getClassName).collect(Collectors.toList());
        List<UUID> classIds = assignment.getClasses().stream().map(SchoolClass::getId).collect(Collectors.toList());

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .classIds(classIds)
                .classNames(classNames)
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .attachmentUrl(assignment.getAttachmentUrl())
                .createdAt(assignment.getCreatedAt())
                .isExpired(isExpired)
                .isExam(assignment.getIsExam())
                .isPublished(assignment.getIsPublished())
                .build();
    }
}
