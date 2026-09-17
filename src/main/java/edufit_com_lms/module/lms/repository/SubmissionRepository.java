package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    Optional<Submission> findByAssignmentIdAndStudentId(UUID assignmentId, Long studentId);

    List<Submission> findByAssignmentId(UUID assignmentId);

    Page<Submission> findByAssignmentId(UUID assignmentId,
                                        Pageable pageable);

    List<Submission> findByStudentId(Long studentId);

    void deleteByAssignmentId(UUID assignmentId);

    @Query(value = "SELECT COUNT(s.id) FROM submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "JOIN school_classes sc ON a.class_id = sc.id " +
            "WHERE sc.lecturer_id = :lecturerId AND s.score IS NULL", nativeQuery = true)
    long countPendingGradingByLecturer(@Param("lecturerId") Long lecturerId);

    List<Submission> findByStudentIdAndAssignmentIdIn(@Param("studentId") Long studentId, List<UUID> assignmentIds);
}
