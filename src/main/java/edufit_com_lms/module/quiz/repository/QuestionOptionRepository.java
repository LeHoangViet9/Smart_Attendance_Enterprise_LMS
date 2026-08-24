package edufit_com_lms.module.quiz.repository;

import edufit_com_lms.module.quiz.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption,Long> {
}
