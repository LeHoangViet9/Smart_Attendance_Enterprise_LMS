package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Courses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Courses, UUID> {
    List<Courses> findByLecturerId(Long lecturerId);
}
