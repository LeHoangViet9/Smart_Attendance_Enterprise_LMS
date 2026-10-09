package edufit_com_lms.module.quiz.repository;

import edufit_com_lms.module.quiz.entity.QuizAttempt;
import edufit_com_lms.module.quiz.entity.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    // Check xem sinh viÃªn cÃ³ Ä‘ang thi dá»Ÿ bÃ i nÃ y khÃ´ng
    List<QuizAttempt> findByQuizIdAndStudentUserId(Long quizId, Long studentId);

    // Láº¥y lá»‹ch sá»­ thi cá»§a má»™t há»c sinh
    Page<QuizAttempt> findAllByStudentUserIdOrderByStartTimeDesc(Long studentId, Pageable pageable);

    // Láº¥y danh sÃ¡ch bÃ i lÃ m cá»§a má»™t Quiz (cho giáº£ng viÃªn cháº¥m bÃ i)
    Page<QuizAttempt> findAllByQuizIdOrderByStartTimeDesc(Long quizId, Pageable pageable);
    List<QuizAttempt> findByQuizId(Long quizId);
    boolean existsByQuizIdAndStatus(Long quizId, QuizStatus status);
    
    // Láº¥y cÃ¡c attempt Ä‘ang IN_PROGRESS Ä‘á»ƒ kiá»ƒm tra timeout
    List<QuizAttempt> findByStatus(QuizStatus status);
    Page<QuizAttempt> findByStatus(QuizStatus status, Pageable pageable);
}
