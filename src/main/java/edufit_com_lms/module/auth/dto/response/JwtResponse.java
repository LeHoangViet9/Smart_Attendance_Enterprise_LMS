package edufit_com_lms.module.auth.dto.response;

import edufit_com_lms.module.auth.entity.TypeToken;

import javax.management.relation.Role;

public class JwtResponse {
    private String token;
    private String email;
    private Role role;
    private TypeToken tokenType;
}
