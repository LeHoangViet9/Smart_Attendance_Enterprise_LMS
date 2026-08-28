package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.response.LecturerStatsResponse;

public interface LecturerService {
    LecturerStatsResponse getLecturerStats(Long lecturerId);
}
