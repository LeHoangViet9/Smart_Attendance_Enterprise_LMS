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

    // Náº±m trong cÃ¢u há»i nÃ o
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Ná»™i dung Ä‘Ã¡p Ã¡n (Hoáº·c chá»©a "keyword Ä‘Ã¡p Ã¡n" náº¿u lÃ  cÃ¢u Ä‘iá»n chá»¯)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Biáº¿n cá» mÃ¡u chá»‘t giáº¥u React (True náº¿u Ä‘Ã¢y lÃ  cÃ¢u Ä‘Ãºng)
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
}
