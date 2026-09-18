package edufit_com_lms.module.attendance.repository;

import edufit_com_lms.module.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {
    List<AttendanceRecord> findBySchoolClassId(UUID classId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.schoolClass.lecturer.userId = :lecturerId")
    long countTotalAttendanceByLecturer(@Param("lecturerId") Long lecturerId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.schoolClass.lecturer.userId = :lecturerId AND a.status = 'PRESENT'")
    long countPresentAttendanceByLecturer(@Param("lecturerId") Long lecturerId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId AND a.student.userId = :studentId")
    long countTotalAttendanceByClassAndStudent(@Param("classId") UUID classId, @Param("studentId") Long studentId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId AND a.student.userId = :studentId AND a.status = 'PRESENT'")
    long countPresentAttendanceByClassAndStudent(@Param("classId") UUID classId, @Param("studentId") Long studentId);

    @Query("SELECT a.student.userId, COUNT(a.id) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId AND a.status = 'PRESENT' GROUP BY a.student.userId")
    List<Object[]> countPresentAttendanceByClassIdGroupedByStudent(@Param("classId") UUID classId);

    @Query("SELECT COUNT(DISTINCT a.checkInTime) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId")
    long countDistinctCheckInTimeBySchoolClassId(@Param("classId") UUID classId);
}
