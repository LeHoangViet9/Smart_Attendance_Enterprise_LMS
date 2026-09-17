package edufit_com_lms.module.quiz.repository;

import edufit_com_lms.module.quiz.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
        // Tìm các bài quiz chưa tới giờ khóa
        List<Quiz> findByEndTimeAfterOrEndTimeIsNull(LocalDateTime now);

        // Dùng cho tìm kiếm nhiều trường (title, description, hoặc thời gian)
        @Query("SELECT q FROM Quiz q WHERE " +
                        "(:majorId IS NULL OR q.major.id = :majorId) AND " +
                        "(:keyword IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR CAST(q.timeLimitMinutes AS string) LIKE CONCAT('%', :keyword, '%'))")
        Page<Quiz> searchByKeyword(@Param("keyword") String keyword,
                        @Param("majorId") java.util.UUID majorId,
                        Pageable pageable);
}
