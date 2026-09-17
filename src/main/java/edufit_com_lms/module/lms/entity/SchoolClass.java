package edufit_com_lms.module.lms.entity;

import edufit_com_lms.module.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "school_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "class_name", nullable = false, unique = true)
    private String className;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @Column(name = "entry_year")
    private Integer entryYear;

    /**
     * Giảng viên phụ trách Lớp học phần này (BRD: Một Class có ít nhất một Lecturer phụ trách)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private User lecturer;

    /**
     * Môn học mà Lớp học phần này thuộc về (BRD: Một Course có thể có nhiều Class)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Courses course;
}
