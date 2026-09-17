package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessionRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(UUID courseId);

    Page<Lesson> findByCourseIdOrderByOrderIndexAsc(UUID courseId,
                                                    Pageable pageable);
}
