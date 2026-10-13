package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(UUID courseId);

    Page<Lesson> findByCourseIdOrderByOrderIndexAsc(UUID courseId,
                                                    Pageable pageable);

    long countByCourseId(UUID courseId);

    @Query("SELECT l.courseId, COUNT(l.id) FROM Lesson l WHERE l.courseId IN :courseIds GROUP BY l.courseId")
    List<Object[]> countByCourseIds(@Param("courseIds") List<UUID> courseIds);
}
