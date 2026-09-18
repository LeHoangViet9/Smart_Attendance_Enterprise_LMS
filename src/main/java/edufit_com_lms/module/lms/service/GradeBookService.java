package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.request.UpdateGradebookRequest;
import edufit_com_lms.module.lms.dto.response.GradebookItemResponse;

import java.util.List;
import java.util.UUID;

public interface GradebookService {
    List<GradebookItemResponse> getGradebookForClass(UUID classId, Long lecturerId);
    
    List<GradebookItemResponse> syncGradebookForClass(UUID classId, Long lecturerId);
    
    void updateGradebook(UUID gradebookId, UpdateGradebookRequest request, Long lecturerId);
    
    List<GradebookItemResponse> getMyGrades(Long studentId);
}
