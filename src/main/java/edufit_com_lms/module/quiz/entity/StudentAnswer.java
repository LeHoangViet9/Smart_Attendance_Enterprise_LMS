package edufit_com_lms.module.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_answers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class StudentAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    // Sinh viên trả lời cho câu hỏi nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // NẾU LÀ TRẮC NGHIỆM: Sinh viên chọn Option nào (Thằng này null nếu là fill in
    // the blank)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    // NẾU LÀ ĐIỀN VÀO CHỖ TRỐNG: Lưu thẳng câu chữ sinh viên tự gõ vào đây
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    // Câu này được hệ thống quy kết là Đúng hay Sai để tính điểm?
    @Column(name = "is_awarded")
    private Boolean isAwarded;
}
