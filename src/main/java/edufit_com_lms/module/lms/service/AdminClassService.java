package edufit_com_lms.module.lms.service;

import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.StudentProfile;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.StudentProfileRepository;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.dto.response.SchoolClassResponse;
import edufit_com_lms.module.lms.entity.Major;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.MajorRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminClassService {
    private final SchoolClassRepository schoolClassRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final MajorRepository majorRepository;

    @Transactional(readOnly = true)
    public List<SchoolClassResponse> getAllClasses() {
        return schoolClassRepository.findAll().stream().map(c -> {
            return SchoolClassResponse.builder()
                    .id(c.getId())
                    .className(c.getClassName())
                    .majorName(c.getMajor() != null ? c.getMajor().getName() : "N/A")
                    .entryYear(c.getEntryYear())
                    .homeroomLecturerId(c.getHomeroomLecturer() != null ? c.getHomeroomLecturer().getUserId() : null)
                    .homeroomLecturerName(
                            c.getHomeroomLecturer() != null ? c.getHomeroomLecturer().getFullName() : null)
                    .studentCount(studentProfileRepository.countBySchoolClass(c))
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void assignHomeroomLecturer(UUID classId, Long lecturerId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFound("Class not found"));

        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFound("Lecturer not found"));

        if (lecturer.getRole() != Role.LECTURER) {
            throw new IllegalArgumentException("User must be a lecturer");
        }

        schoolClass.setHomeroomLecturer(lecturer);
        schoolClassRepository.save(schoolClass);
    }

    @Transactional
    public void autoAssignStudents() {
        List<StudentProfile> unassigned = studentProfileRepository.findUnassignedStudents();
        List<Major> majors = majorRepository.findAll();
        if (majors.isEmpty() || unassigned.isEmpty())
            return;

        Random random = new Random();
        Map<UUID, List<SchoolClass>> majorToClassesMap = new HashMap<>();
        Map<SchoolClass, Integer> classCountMap = new HashMap<>();

        // Cache classes and counts
        for (Major major : majors) {
            List<SchoolClass> classes = schoolClassRepository.findByMajorId(major.getId());
            majorToClassesMap.put(major.getId(), new ArrayList<>(classes));
            for (SchoolClass c : classes) {
                classCountMap.put(c, studentProfileRepository.countBySchoolClass(c));
            }
        }

        List<StudentProfile> profilesToSave = new ArrayList<>();
        List<SchoolClass> classesToSave = new ArrayList<>();

        for (StudentProfile sp : unassigned) {
            Major major = majors.get(random.nextInt(majors.size()));
            List<SchoolClass> classesForMajor = majorToClassesMap.get(major.getId());

            SchoolClass targetClass = null;
            for (SchoolClass c : classesForMajor) {
                if (classCountMap.getOrDefault(c, 0) < 30) {
                    targetClass = c;
                    break;
                }
            }

            if (targetClass == null) {
                targetClass = SchoolClass.builder()
                        .className(major.getCode() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
                        .major(major)
                        .entryYear(2024)
                        .build();
                classesToSave.add(targetClass);
                classesForMajor.add(targetClass);
                classCountMap.put(targetClass, 0);
            }

            classCountMap.put(targetClass, classCountMap.get(targetClass) + 1);
            sp.setSchoolClass(targetClass);
            profilesToSave.add(sp);
        }

        if (!classesToSave.isEmpty()) {
            schoolClassRepository.saveAll(classesToSave);
        }
        if (!profilesToSave.isEmpty()) {
            studentProfileRepository.saveAll(profilesToSave);
        }
    }

    @Transactional
    public void autoAssignLecturers() {
        List<SchoolClass> allIncludedClasses = schoolClassRepository.findAll();
        List<SchoolClass> classesWithoutLecturer = allIncludedClasses.stream()
                .filter(c -> c.getHomeroomLecturer() == null && c.getMajor() != null)
                .collect(Collectors.toList());

        Set<Long> assignedLecturerIds = allIncludedClasses.stream()
                .filter(c -> c.getHomeroomLecturer() != null)
                .map(c -> c.getHomeroomLecturer().getUserId())
                .collect(Collectors.toSet());

        // We can fetch LECTURER users, assuming we have a custom way.
        // User repository might not have findByRole(Role) explicitly if we used custom
        // queries.
        // Let's rely on finding all users and filtering, as data size is small for this
        // prototype.
        List<User> availableLecturers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.LECTURER && !assignedLecturerIds.contains(u.getUserId()))
                .collect(Collectors.toList());

        List<SchoolClass> classesToUpdate = new ArrayList<>();

        for (SchoolClass c : classesWithoutLecturer) {
            if (availableLecturers.isEmpty())
                break;

            // Try to match by major name
            Optional<User> match = availableLecturers.stream()
                    .filter(l -> l.getLecturerProfile() != null &&
                            l.getLecturerProfile().getMajor() != null &&
                            c.getMajor().getName().toLowerCase()
                                    .contains(l.getLecturerProfile().getMajor().toLowerCase()))
                    .findFirst();

            if (match.isPresent()) {
                c.setHomeroomLecturer(match.get());
                availableLecturers.remove(match.get());
            } else {
                // If no exact match, just pick the first available
                User fallback = availableLecturers.get(0);
                c.setHomeroomLecturer(fallback);
                availableLecturers.remove(fallback);
            }
            classesToUpdate.add(c);
        }

        if (!classesToUpdate.isEmpty()) {
            schoolClassRepository.saveAll(classesToUpdate);
        }
    }
}
