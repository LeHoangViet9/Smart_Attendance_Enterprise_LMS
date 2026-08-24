package edufit_com_lms.module.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionResponse {
    private Long id;
    private String content;
    // BÍ MẬT 100%: Tuyệt đối không chứa field isCorrect ở đây!
}
