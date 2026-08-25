package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Lession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessionRepository extends JpaRepository<Lession, UUID> {
    List<Lession> findByCourseIdOrderByOrderIndexAsc(UUID courseId);
}
