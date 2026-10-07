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

    // Sinh viÃªn tráº£ lá»i cho cÃ¢u há»i nÃ o
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Náº¾U LÃ€ TRáº®C NGHIá»†M: Sinh viÃªn chá»n Option nÃ o (Tháº±ng nÃ y null náº¿u lÃ  fill in
    // the blank)
    // For SINGLE_CHOICE / TRUE_FALSE: the selected option entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    // For MULTIPLE_CHOICE: store selected option IDs as a collection of longs
    @ElementCollection
    @CollectionTable(name = "student_answer_selected_option_ids", joinColumns = @JoinColumn(name = "student_answer_id"))
    @Column(name = "option_id")
    private java.util.Set<Long> selectedOptionIds;


    // Náº¾U LÃ€ ÄIá»€N VÃ€O CHá»– TRá»NG: LÆ°u tháº³ng cÃ¢u chá»¯ sinh viÃªn tá»± gÃµ vÃ o Ä‘Ã¢y
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText; // Used for FILL_BLANK or can store JSON of MULTIPLE_CHOICE ids if needed

    // CÃ¢u nÃ y Ä‘Æ°á»£c há»‡ thá»‘ng quy káº¿t lÃ  ÄÃºng hay Sai Ä‘á»ƒ tÃ­nh Ä‘iá»ƒm?
    @Column(name = "is_awarded")
    private Boolean isAwarded;

    // Äiá»ƒm thá»±c táº¿ Ä‘áº¡t Ä‘Æ°á»£c (dÃ¹ng cho cÃ¢u Tá»± luáº­n cháº¥m tá»«ng pháº§n)
    @Column(name = "earned_points")
    private Double earnedPoints;

    // Nháº­n xÃ©t cá»§a giáº£ng viÃªn (dÃ nh riÃªng cho cÃ¢u Tá»± luáº­n)
    @Column(columnDefinition = "TEXT")
    private String feedback;
}
