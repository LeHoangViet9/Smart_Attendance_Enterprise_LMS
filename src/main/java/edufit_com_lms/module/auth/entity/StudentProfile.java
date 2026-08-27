package edufit_com_lms.module.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class StudentProfile {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "parent_phone")
    private String parentPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id")
    private edufit_com_lms.module.lms.entity.SchoolClass schoolClass;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;
}
