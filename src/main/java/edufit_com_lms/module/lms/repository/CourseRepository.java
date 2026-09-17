package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Courses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository cho Course (Môn học - Danh mục).
 * BRD 8.3: Course là danh mục môn học. Lecturer được gán vào Class, không vào Course.
 */
@Repository
public interface CourseRepository extends JpaRepository<Courses, UUID> {

    Page<Courses> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Courses> findByMajorId(UUID majorId, Pageable pageable);

    Page<Courses> findByMajorIdAndTitleContainingIgnoreCase(UUID majorId, String title, Pageable pageable);
}
