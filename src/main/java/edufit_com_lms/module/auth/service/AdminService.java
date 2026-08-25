package edufit_com_lms.module.auth.service;

import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.dto.response.AdminStatsResponse;
import java.util.List;

public interface AdminService {
    AdminStatsResponse getDashboardStats();

    List<UserResponse> getAllUsersExcludingAdmins();
}
