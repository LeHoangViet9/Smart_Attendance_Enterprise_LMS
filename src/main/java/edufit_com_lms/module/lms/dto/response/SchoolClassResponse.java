package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO response cho Lá»›p há»c pháº§n (Class).
 * BRD 8.3: Má»™t Class cÃ³ Lecturer phá»¥ trÃ¡ch vÃ  thuá»™c vá» má»™t Course.
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

    // BRD: Lecturer phá»¥ trÃ¡ch lá»›p há»c pháº§n
    private Long lecturerId;
    private String lecturerName;

    // BRD: MÃ´n há»c mÃ  lá»›p nÃ y thuá»™c vá»
    private UUID courseId;
    private String courseName;

    private Integer studentCount;
}
