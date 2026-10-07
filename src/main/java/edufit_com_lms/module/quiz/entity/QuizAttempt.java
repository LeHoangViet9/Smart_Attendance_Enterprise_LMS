package edufit_com_lms.module.quiz.entity;

import edufit_com_lms.module.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BÃ i Ä‘Æ°á»£c lÃ m cho Ä‘á» thi nÃ o
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Ai lÃ  ngÆ°á»i lÃ m
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Äiá»ƒm sá»‘ nháº­n Ä‘Æ°á»£c lÆ°u cá»©ng vÃ o Ä‘Ã¢y
    private Double score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status;

    @Column(name = "proctoring_image_url")
    private String proctoringImageUrl;

    // Chi tiáº¿t tá»«ng cÃ¢u há»i sinh viÃªn Ä‘Ã£ check
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentAnswer> studentAnswers;
}
