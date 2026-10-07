package edufit_com_lms.module.lms.repository;

import edufit_com_lms.module.lms.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho SchoolClass (Lá»›p há»c pháº§n).
 * BRD 8.3: Má»™t Course cÃ³ thá»ƒ cÃ³ nhiá»u Class. Má»™t Class cÃ³ Ã­t nháº¥t má»™t Lecturer phá»¥ trÃ¡ch.
 */
@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
    Optional<SchoolClass> findByClassName(String className);

    List<SchoolClass> findByMajorId(UUID majorId);

    /** TÃ¬m cÃ¡c Lá»›p há»c pháº§n do Giáº£ng viÃªn phá»¥ trÃ¡ch (theo BRD: Lecturer Assignment) */
    List<SchoolClass> findByLecturer_UserId(Long userId);

    /** TÃ¬m táº¥t cáº£ Lá»›p há»c pháº§n thuá»™c má»™t MÃ´n há»c (BRD: Má»™t Course -> nhiá»u Class) */
    List<SchoolClass> findByCourseId(UUID courseId);

    long countByLecturer(edufit_com_lms.module.auth.entity.User lecturer);
}
