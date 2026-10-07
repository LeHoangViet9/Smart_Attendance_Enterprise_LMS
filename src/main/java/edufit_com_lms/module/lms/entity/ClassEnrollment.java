package edufit_com_lms.module.lms.entity;

import edufit_com_lms.module.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Báº£ng Ä‘Äƒng kÃ½ Sinh viÃªn vÃ o Lá»›p há»c pháº§n (Class).
 * BRD 8.3: Student chá»‰ Ä‘Æ°á»£c truy cáº­p Class mÃ  mÃ¬nh Ä‘Æ°á»£c enrollment.
 */
@Entity
@Table(name = "class_enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_id", "student_user_id"})
})
@SQLDelete(sql = "UPDATE class_enrollments SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Column(name = "enrolled_at", updatable = false)
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
