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
    private Boolean isCorrect; // Bật cờ này để Giáo Viên (Lecturer) trên FE QuizManagement đọc được
}
