package edufit_com_lms.module.lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MÃ´n há»c (Course Catalog) - chá»‰ lÃ  danh má»¥c mÃ´n há»c.
 * BRD 8.3: Má»™t Course cÃ³ thá»ƒ cÃ³ nhiá»u Class.
 * Giáº£ng viÃªn Ä‘Æ°á»£c gÃ¡n vÃ o tá»«ng Class, khÃ´ng gÃ¡n trá»±c tiáº¿p vÃ o Course.
 */
@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courses {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;
}
