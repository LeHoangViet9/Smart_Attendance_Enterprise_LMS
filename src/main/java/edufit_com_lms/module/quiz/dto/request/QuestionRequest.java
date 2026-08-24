package edufit_com_lms.module.quiz.dto.request;

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
public class QuestionRequest {
    private String content;
    private Double points;
    private QuestionType questionType; // Loại câu hỏi

    // Mảng chứa các đáp án (A,B,C,D) hoặc các chữ đáp án mẫu
    private List<OptionRequest> options;
}
