package edufit_com_lms.security;

import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User users = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return CustomUserDetail.builder()
                .id(users.getUserId())
                .email(users.getEmail())

                .passwordHash(users.getPassword())
                .fullName(users.getFullName())
                .isActive(users.getIsActive())
                .authorities(mapToGrandAuthority(users.getRole()))
                .build();
    }

    private Collection<? extends GrantedAuthority> mapToGrandAuthority(Role role) {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
