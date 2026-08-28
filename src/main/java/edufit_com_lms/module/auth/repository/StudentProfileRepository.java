package edufit_com_lms.module.auth.repository;

import edufit_com_lms.module.auth.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    @Query("SELECT sp FROM StudentProfile sp WHERE sp.schoolClass IS NULL")
    List<StudentProfile> findUnassignedStudents();

    int countBySchoolClass(edufit_com_lms.module.lms.entity.SchoolClass schoolClass);

    @Query("SELECT sp FROM StudentProfile sp JOIN FETCH sp.user JOIN FETCH sp.schoolClass WHERE sp.schoolClass.id = :classId")
    List<StudentProfile> findBySchoolClassId(@Param("classId") java.util.UUID classId);
}
