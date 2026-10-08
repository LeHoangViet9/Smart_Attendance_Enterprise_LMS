package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.dto.request.ChatbotRequest;
import edufit_com_lms.module.lms.dto.response.ChatbotResponse;

public interface ChatbotService {
    ChatbotResponse askTutor(ChatbotRequest request, Long studentId);
}
