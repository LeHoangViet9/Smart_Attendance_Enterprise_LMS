package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

/**
 * Repository cho SchoolClass (Lớp học phần).
 * BRD 8.3: Một Course có thể có nhiều Class. Một Class có ít nhất một Lecturer phụ trách.
 */
@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
    Optional<SchoolClass> findByClassName(String className);

    List<SchoolClass> findByMajorId(UUID majorId);

    /** Tìm các Lớp học phần do Giảng viên phụ trách (theo BRD: Lecturer Assignment) */
    List<SchoolClass> findByLecturer_UserId(Long userId);

    /** Tìm tất cả Lớp học phần thuộc một Môn học (BRD: Một Course -> nhiều Class) */
    List<SchoolClass> findByCourseId(UUID courseId);
}
