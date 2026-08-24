package edufit_com_lms.module.quiz.mapper;

import edufit_com_lms.module.quiz.dto.request.QuizRequest;
import edufit_com_lms.module.quiz.dto.response.OptionResponse;
import edufit_com_lms.module.quiz.dto.response.QuestionResponse;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.entity.QuestionOption;
import edufit_com_lms.module.quiz.entity.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuizMapper {

    QuizResponse toResponse(Quiz quiz);

    QuestionResponse toQuestionResponse(Question question);

    OptionResponse toOptionResponse(QuestionOption option);

    Quiz toEntity(QuizRequest request);
}
