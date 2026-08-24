package edufit_com_lms.module.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentAnswerRequest {
    private Long questionId;
    private Long selectedOptionId; // Gắn ID nếu chọn trắc nghiệm A B C D
    private String answerText; // Gắn text nếu là câu dạng Điền chữ (Fill in the blank)
}
