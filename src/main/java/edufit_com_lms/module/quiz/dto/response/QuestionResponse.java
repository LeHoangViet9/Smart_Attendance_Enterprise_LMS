package edufit_com_lms.module.quiz.dto.response;

import edufit_com_lms.module.quiz.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private Long id;
    private String content;
    private Double points;
    private QuestionType questionType;
    private List<OptionResponse> options; // Danh sách Câu C Trắc Nghiệm sẽ được nhét vào đây
}
