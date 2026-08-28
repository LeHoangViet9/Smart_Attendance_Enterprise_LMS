package edufit_com_lms.module.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = true;
}
