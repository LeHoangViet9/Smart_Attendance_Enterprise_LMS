package edufit_com_lms.module.attendance.repository;

import edufit_com_lms.module.attendance.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {

    Page<AttendanceRecord> findBySchoolClassId(UUID classId, Pageable pageable);

    Page<AttendanceRecord> findBySchoolClassIdAndCheckInTimeBetween(UUID classId, java.time.LocalDateTime start, java.time.LocalDateTime end, Pageable pageable);

    Page<AttendanceRecord> findByStudentUserId(Long studentId, Pageable pageable);

    Page<AttendanceRecord> findByStudentUserIdAndCheckInTimeBetween(Long studentId, java.time.LocalDateTime start, java.time.LocalDateTime end, Pageable pageable);

    boolean existsBySchoolClassIdAndCheckInTimeBetween(UUID classId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.schoolClass.lecturer.userId = :lecturerId")
    long countTotalAttendanceByLecturer(@Param("lecturerId") Long lecturerId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.schoolClass.lecturer.userId = :lecturerId AND a.status = 'PRESENT'")
    long countPresentAttendanceByLecturer(@Param("lecturerId") Long lecturerId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId")
    long countTotalAttendanceByClassId(@Param("classId") UUID classId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId AND a.status = 'PRESENT'")
    long countPresentAttendanceByClassId(@Param("classId") UUID classId);

    @Query("SELECT a.student.userId, COUNT(a.id) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId AND a.status = 'PRESENT' GROUP BY a.student.userId")
    List<Object[]> countPresentAttendanceByClassIdGroupedByStudent(@Param("classId") UUID classId);

    @Query("SELECT a.student.userId, COUNT(a.id), SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId GROUP BY a.student.userId")
    List<Object[]> countAttendanceByClassIdGroupedByStudent(@Param("classId") UUID classId);

    @Query("SELECT COUNT(DISTINCT a.checkInTime) FROM AttendanceRecord a WHERE a.schoolClass.id = :classId")
    long countDistinctCheckInTimeBySchoolClassId(@Param("classId") UUID classId);
}
