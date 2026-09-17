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
    @Mapping(target = "major", expression = "java(user.getRole() == edufit_com_lms.module.auth.entity.Role.LECTURER && user.getLecturerProfile() != null && user.getLecturerProfile().getMajor() != null ? user.getLecturerProfile().getMajor().getName() : (user.getRole() == edufit_com_lms.module.auth.entity.Role.STUDENT && user.getStudentProfile() != null && user.getStudentProfile().getMajor() != null ? user.getStudentProfile().getMajor().getName() : (user.getRole() == edufit_com_lms.module.auth.entity.Role.STUDENT && user.getStudentProfile() != null && user.getStudentProfile().getSchoolClass() != null && user.getStudentProfile().getSchoolClass().getMajor() != null ? user.getStudentProfile().getSchoolClass().getMajor().getName() : null)))")
    @Mapping(source = "lecturerProfile.department", target = "department")
    UserResponse toUserResponse(User user);

}
