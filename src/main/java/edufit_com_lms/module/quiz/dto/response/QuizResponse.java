package edufit_com_lms.module.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private LocalDateTime startTime;
    private Boolean requiresProctoring;
    private List<QuestionResponse> questions; // Toàn bộ đề thi được nhồi vào cục này để ném về React
    private UUID majorId;
    private String majorName;
    private Long createdBy;
}
