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
    private QuestionType questionType; // Loáº¡i cÃ¢u há»i

    // Máº£ng chá»©a cÃ¡c Ä‘Ã¡p Ã¡n (A,B,C,D) hoáº·c cÃ¡c chá»¯ Ä‘Ã¡p Ã¡n máº«u
    private List<OptionRequest> options;
}
