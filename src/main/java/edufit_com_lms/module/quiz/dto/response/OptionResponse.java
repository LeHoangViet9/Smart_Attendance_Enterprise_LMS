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
    private Boolean isCorrect; // Báº­t cá» nÃ y Ä‘á»ƒ GiÃ¡o ViÃªn (Lecturer) trÃªn FE QuizManagement Ä‘á»c Ä‘Æ°á»£c
}
