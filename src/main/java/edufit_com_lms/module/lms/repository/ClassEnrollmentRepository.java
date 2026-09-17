package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.ClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository cho ClassEnrollment.
 * BRD 8.3: Sinh viên đăng ký vào Lớp học phần (Class), không phải Course.
 */
@Repository
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, UUID> {

    List<ClassEnrollment> findBySchoolClassId(UUID classId);

    boolean existsBySchoolClassIdAndStudentUserId(UUID classId, Long studentUserId);

    List<ClassEnrollment> findByStudentUserId(Long studentUserId);
}
