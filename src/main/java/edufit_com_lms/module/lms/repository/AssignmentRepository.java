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
    List<Assignment> findByClasses_Id(UUID classId);

    Page<Assignment> findByClasses_Id(UUID classId, Pageable pageable);

    Page<Assignment> findDistinctByClasses_IdIn(List<UUID> classIds, Pageable pageable);
}
