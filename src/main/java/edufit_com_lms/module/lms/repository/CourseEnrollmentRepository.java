package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {
    List<CourseEnrollment> findByCourseId(UUID courseId);

    boolean existsByCourseIdAndStudentUserId(UUID courseId, Long studentUserId);
}
