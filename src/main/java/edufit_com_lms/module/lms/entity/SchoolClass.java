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
     * Giáº£ng viÃªn phá»¥ trÃ¡ch Lá»›p há»c pháº§n nÃ y (BRD: Má»™t Class cÃ³ Ã­t nháº¥t má»™t Lecturer phá»¥ trÃ¡ch)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private User lecturer;

    /**
     * MÃ´n há»c mÃ  Lá»›p há»c pháº§n nÃ y thuá»™c vá» (BRD: Má»™t Course cÃ³ thá»ƒ cÃ³ nhiá»u Class)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Courses course;
}
