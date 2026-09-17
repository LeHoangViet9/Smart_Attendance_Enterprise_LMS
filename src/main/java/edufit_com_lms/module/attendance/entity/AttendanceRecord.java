package edufit_com_lms.module.attendance.entity;

import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.lms.entity.SchoolClass;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "check_in_time")
    @Builder.Default
    private LocalDateTime checkInTime = LocalDateTime.now();

    @Column(name = "status", nullable = false)
    private String status; // "PRESENT", "ABSENT"
}
