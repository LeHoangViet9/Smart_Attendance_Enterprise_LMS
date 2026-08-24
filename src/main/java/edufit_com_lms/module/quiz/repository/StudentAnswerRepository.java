package edufit_com_lms.module.quiz.repository;

import edufit_com_lms.module.quiz.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findAllByAttemptId(Long attemptId);
}
