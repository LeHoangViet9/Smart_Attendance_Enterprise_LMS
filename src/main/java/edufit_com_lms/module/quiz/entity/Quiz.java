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
@Table(name = "quizzes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Sá»‘ láº§n lÃ m tá»‘i Ä‘a (null = khÃ´ng giá»›i háº¡n)
    @Column(name = "max_attempts")
    private Integer maxAttempts;

    // Giá» má»Ÿ vÃ  khÃ³a bÃ i thi
    // Thá»i gian lÃ m bÃ i tÃ­nh báº±ng phÃºt
    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Trá» tá»›i Giáº£ng viÃªn / Admin ngÆ°á»i táº¡o Ä‘á»
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    // Trá» tá»›i ChuyÃªn ngÃ nh (Quiz thuá»™c chuyÃªn ngÃ nh nÃ o)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private edufit_com_lms.module.lms.entity.Major major;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "quiz_classes",
        joinColumns = @JoinColumn(name = "quiz_id"),
        inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    private java.util.List<edufit_com_lms.module.lms.entity.SchoolClass> classes = new java.util.ArrayList<>();

    // Danh sÃ¡ch CÃ¢u há»i (XÃ³a quiz thÃ¬ xÃ³a luÃ´n dÃ n cÃ¢u há»i)
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @Column(name = "show_score")
    private Boolean showScore;

    @Column(name = "review_type")
    @Enumerated(EnumType.STRING)
    private ReviewType reviewType;

    @Column(name = "requires_proctoring")
    @Builder.Default
    private Boolean requiresProctoring = false;

    // Máº­t kháº©u Ä‘á» thi (náº¿u giáº£ng viÃªn muá»‘n cÃ i Ä‘áº·t)
    @Column(name = "access_code")
    private String accessCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
