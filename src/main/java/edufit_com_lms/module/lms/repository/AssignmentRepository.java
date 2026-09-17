package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    List<Assignment> findByClassId(UUID classId);

    Page<Assignment> findByClassId(UUID classId, Pageable pageable);

    Page<Assignment> findByClassIdIn(List<UUID> classIds, Pageable pageable);
}
