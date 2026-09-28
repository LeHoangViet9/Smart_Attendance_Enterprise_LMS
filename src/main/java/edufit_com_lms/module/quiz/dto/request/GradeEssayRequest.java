package edufit_com_lms.module.quiz.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class GradeEssayRequest {

    @NotNull
    private List<QuestionGrade> grades;

    @Data
    public static class QuestionGrade {
        @NotNull
        private Long studentAnswerId;
        
        @NotNull
        private Double points;

        private String feedback;
    }
}
