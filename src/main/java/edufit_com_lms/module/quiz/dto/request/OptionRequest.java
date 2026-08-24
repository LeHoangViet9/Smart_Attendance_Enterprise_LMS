package edufit_com_lms.module.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionRequest {
    private String content; // Nội dung đáp án (vd: Bằng 2)
    private Boolean isCorrect; // Phải CÓ biến này để Admin đánh dấu câu đúng sai!
}
