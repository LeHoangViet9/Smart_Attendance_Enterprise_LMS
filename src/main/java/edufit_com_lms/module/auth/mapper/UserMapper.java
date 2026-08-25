package edufit_com_lms.module.auth.mapper;

import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.quiz.dto.response.AdminStatsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponse toUserResponse(User user);
    User toEntity(AdminStatsResponse userRequest);
}
