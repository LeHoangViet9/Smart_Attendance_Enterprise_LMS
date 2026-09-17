package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Gradebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradebookRepository extends JpaRepository<Gradebook, UUID> {

    @Query("SELECT g FROM Gradebook g WHERE g.enrollment.schoolClass.id = :classId")
    List<Gradebook> findByClassId(@Param("classId") UUID classId);

    @Query("SELECT g FROM Gradebook g WHERE g.enrollment.student.id = :studentId")
    List<Gradebook> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT g FROM Gradebook g WHERE g.enrollment.id = :enrollmentId")
    Optional<Gradebook> findByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
}
