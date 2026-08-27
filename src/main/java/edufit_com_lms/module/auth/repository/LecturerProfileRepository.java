package edufit_com_lms.module.auth.repository;

import edufit_com_lms.module.auth.entity.LecturerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, Long> {
}
