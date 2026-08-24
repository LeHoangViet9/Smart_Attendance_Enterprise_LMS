package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.module.lms.dto.GradeSubmissionRequest;
import edufit_com_lms.module.lms.dto.SubmitAssignmentRequest;
import edufit_com_lms.module.lms.dto.SubmissionResponse;
import edufit_com_lms.module.lms.entity.Assignment;
import edufit_com_lms.module.lms.entity.Submission;
import edufit_com_lms.common.exception.BadRequestException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.lms.repository.AssignmentRepository;
import edufit_com_lms.module.lms.repository.SubmissionRepository;
import edufit_com_lms.module.lms.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;

    @Override
    public SubmissionResponse submitAssignment(UUID assignmentId, SubmitAssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFound("Assignment not found with ID: " + assignmentId));

        Optional<Submission> existingSubmission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, request.getStudentId());

        Submission submission;
        LocalDateTime now = LocalDateTime.now();

        if (existingSubmission.isPresent()) {
            submission = existingSubmission.get();
            submission.setFileUrl(request.getFileUrl());
            submission.setSubmittedAt(now);
            log.info("Student {} updated submission for assignment {}", request.getStudentId(), assignmentId);
        } else {
            submission = Submission.builder()
                    .assignmentId(assignmentId)
                    .studentId(request.getStudentId())
                    .fileUrl(request.getFileUrl())
                    .submittedAt(now)
                    .build();
            log.info("Student {} submitted a new assignment for {}", request.getStudentId(), assignmentId);
        }

        Submission saved = submissionRepository.save(submission);
        return mapToResponse(saved, assignment);
    }

    @Override
    public SubmissionResponse gradeSubmission(UUID submissionId, GradeSubmissionRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFound("Submission not found with ID: " + submissionId));

        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFound("Associated assignment not found"));

        double maxScore = assignment.getMaxScore() != null ? assignment.getMaxScore() : 10.0;
        if (request.getScore() < 0 || request.getScore() > maxScore) {
            throw new BadRequestException("Score (" + request.getScore() + ") must be between 0 and " + maxScore);
        }

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());

        Submission updated = submissionRepository.save(submission);
        log.info("Graded submission ID {}: score={}", submissionId, request.getScore());

        return mapToResponse(updated, assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(UUID id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Submission not found with ID: " + id));

        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElse(null);

        return mapToResponse(submission, assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionByAssignmentAndStudent(UUID assignmentId, UUID studentId) {
        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFound("Student has not submitted this assignment yet."));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElse(null);

        return mapToResponse(submission, assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByAssignment(UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFound("Assignment not found with ID: " + assignmentId));

        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(sub -> mapToResponse(sub, assignment))
                .collect(Collectors.toList());
    }

    private SubmissionResponse mapToResponse(Submission submission, Assignment assignment) {
        boolean isGraded = submission.getScore() != null;
        boolean isLate = false;

        if (assignment != null && assignment.getDueDate() != null) {
            isLate = submission.getSubmittedAt().isAfter(assignment.getDueDate());
        }

        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .fileUrl(submission.getFileUrl())
                .submittedAt(submission.getSubmittedAt())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .isGraded(isGraded)
                .isLate(isLate)
                .build();
    }
}
