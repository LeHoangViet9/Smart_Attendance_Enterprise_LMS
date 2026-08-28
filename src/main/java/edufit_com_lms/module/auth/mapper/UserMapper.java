package edufit_com_lms.module.auth.mapper;

import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(source = "studentProfile.parentPhone", target = "parentPhone")
    @Mapping(source = "studentProfile.schoolClass.className", target = "className")
    @Mapping(source = "studentProfile.enrollmentYear", target = "enrollmentYear")
    @Mapping(source = "lecturerProfile.degree", target = "degree")
    @Mapping(source = "lecturerProfile.major", target = "major")
    @Mapping(source = "lecturerProfile.department", target = "department")
    UserResponse toUserResponse(User user);

}
