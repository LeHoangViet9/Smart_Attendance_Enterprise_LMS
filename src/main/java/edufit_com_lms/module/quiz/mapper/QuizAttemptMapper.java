package edufit_com_lms.module.quiz.mapper;

import edufit_com_lms.module.quiz.dto.response.QuizAttemptResponse;
import edufit_com_lms.module.quiz.entity.QuizAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuizAttemptMapper {

    @Mapping(source = "quiz.id", target = "quizId")
    @Mapping(source = "student.userId", target = "studentId")
    QuizAttemptResponse toResponse(QuizAttempt attempt);
}
