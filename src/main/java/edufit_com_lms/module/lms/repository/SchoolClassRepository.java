package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
    Optional<SchoolClass> findByClassName(String className);

    List<SchoolClass> findByMajorId(UUID majorId);

    @org.springframework.data.jpa.repository.Query("SELECT sc FROM SchoolClass sc LEFT JOIN FETCH sc.major WHERE sc.homeroomLecturer.userId = :userId")
    List<SchoolClass> findByHomeroomLecturer_UserId(
            @org.springframework.data.repository.query.Param("userId") Long userId);
}
