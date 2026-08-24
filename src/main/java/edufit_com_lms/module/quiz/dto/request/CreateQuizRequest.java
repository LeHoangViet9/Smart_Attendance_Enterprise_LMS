package edufit_com_lms.module.quiz.dto.request;

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
public class CreateQuizRequest {
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Mảng dữ liệu móng vuốt chứa tất tần tật câu hỏi
    private List<QuestionRequest> questions;
}
