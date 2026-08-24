package edufit_com_lms.module.quiz.repository;

import edufit_com_lms.module.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    // Check xem sinh viên có đang thi dở bài này không
    Optional<QuizAttempt> findByQuizIdAndStudentUserId(Long quizId, Long studentId);

    // Lấy lịch sử thi của một học sinh
    List<QuizAttempt> findAllByStudentUserIdOrderByStartTimeDesc(Long studentId);
}
