package edufit_com_lms.module.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private LocalDateTime endTime;
    private List<QuestionResponse> questions; // Toàn bộ đề thi được nhồi vào cục này để ném về React
}
