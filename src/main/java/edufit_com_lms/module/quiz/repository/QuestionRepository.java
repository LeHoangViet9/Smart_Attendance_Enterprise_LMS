package edufit_com_lms.module.quiz.repository;

import edufit_com_lms.module.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId")
    List<Question> findQuestionsWithOptionsByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND " +
            "(:keyword IS NULL OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    org.springframework.data.domain.Page<Question> findByQuizIdAndKeyword(@Param("quizId") Long quizId,
            @Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);
}
