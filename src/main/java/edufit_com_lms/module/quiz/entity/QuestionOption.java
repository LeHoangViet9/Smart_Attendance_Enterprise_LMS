package edufit_com_lms.module.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question_options")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nằm trong câu hỏi nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Nội dung đáp án (Hoặc chứa "keyword đáp án" nếu là câu điền chữ)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Biến cờ máu chốt giấu React (True nếu đây là câu đúng)
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
}
