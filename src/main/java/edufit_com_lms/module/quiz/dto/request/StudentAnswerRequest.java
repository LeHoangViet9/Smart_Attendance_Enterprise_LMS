package edufit_com_lms.module.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentAnswerRequest {
    private Long questionId;
    // Used for SINGLE_CHOICE / TRUE_FALSE (single option ID)
    private Long selectedOptionId;
    // Used for MULTIPLE_CHOICE (list of option IDs)
    private List<Long> selectedOptionIds;
    private String answerText; // for FILL_BLANK
}
