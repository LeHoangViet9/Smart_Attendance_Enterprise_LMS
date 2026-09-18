package edufit_com_lms.module.attendance.service;

import edufit_com_lms.module.attendance.dto.response.FaceOnboardingResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StudentProfileService {
    FaceOnboardingResponse onboardFace(String email, MultipartFile file);

}
