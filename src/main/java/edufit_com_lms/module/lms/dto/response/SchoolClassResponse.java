package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO response cho Lớp học phần (Class).
 * BRD 8.3: Một Class có Lecturer phụ trách và thuộc về một Course.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassResponse {
    private UUID id;
    private String className;
    private String majorName;
    private Integer entryYear;

    // BRD: Lecturer phụ trách lớp học phần
    private Long lecturerId;
    private String lecturerName;

    // BRD: Môn học mà lớp này thuộc về
    private UUID courseId;
    private String courseName;

    private Integer studentCount;
}
