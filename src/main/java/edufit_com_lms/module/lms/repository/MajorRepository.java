package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface MajorRepository extends JpaRepository<Major, UUID> {
    Optional<Major> findByCode(String code);
}
