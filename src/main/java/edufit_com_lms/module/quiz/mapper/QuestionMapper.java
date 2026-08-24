package edufit_com_lms.module.quiz.mapper;

import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import edufit_com_lms.module.quiz.dto.response.QuestionResponse;
import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.entity.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper {
    QuestionResponse toResponse(Question question);
    Quiz toEntity(QuestionRequest request);
}
