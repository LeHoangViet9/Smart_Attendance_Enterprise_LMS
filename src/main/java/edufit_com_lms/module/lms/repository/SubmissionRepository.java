package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    Optional<Submission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);
    List<Submission> findByAssignmentId(UUID assignmentId);
    List<Submission> findByStudentId(UUID studentId);
    void deleteByAssignmentId(UUID assignmentId);
}

