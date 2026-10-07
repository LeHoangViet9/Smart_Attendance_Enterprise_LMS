package edufit_com_lms.module.auth.service;

import edufit_com_lms.module.auth.dto.response.AdminStatsResponse;

public interface AdminService {
    AdminStatsResponse getDashboardStats();
}
