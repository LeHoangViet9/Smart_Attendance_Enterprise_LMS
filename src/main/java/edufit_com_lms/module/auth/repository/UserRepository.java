package edufit_com_lms.module.auth.repository;

import edufit_com_lms.module.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import edufit_com_lms.module.auth.entity.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);

        boolean existsByEmail(String email);

        boolean existsByCode(String code);

        Optional<User> findByCode(String code);

        List<User> findByRoleNot(Role role);

        @Query("SELECT u FROM User u WHERE " +
                        "u.role != edufit_com_lms.module.auth.entity.Role.ADMIN " +
                        "AND (u.role = :role) " +
                        "AND (:isActive IS NULL OR u.isActive = :isActive) " +
                        "AND (:keyword IS NULL OR :keyword = '' OR " +
                        "  LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "  LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "  LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<User> searchUsersWithRole(@Param("keyword") String keyword,
                        @Param("role") Role role,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        @Query("SELECT u FROM User u WHERE " +
                        "u.role != edufit_com_lms.module.auth.entity.Role.ADMIN " +
                        "AND (:isActive IS NULL OR u.isActive = :isActive) " +
                        "AND (:keyword IS NULL OR :keyword = '' OR " +
                        "  LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "  LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "  LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<User> searchUsersWithoutRole(@Param("keyword") String keyword,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);
}
