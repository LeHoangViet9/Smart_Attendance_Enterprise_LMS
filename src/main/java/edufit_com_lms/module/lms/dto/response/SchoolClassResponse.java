package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassResponse {
    private UUID id;
    private String className;
    private String majorName;
    private Integer entryYear;
    private Long homeroomLecturerId;
    private String homeroomLecturerName;
    private Integer studentCount;
}
