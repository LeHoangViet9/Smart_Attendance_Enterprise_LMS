import re

file_path = 'src/main/java/edufit_com_lms/module/lms/service/impl/AssignmentServiceImpl.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace validateLecturerOwnership
content = re.sub(
    r'private void validateLecturerOwnership\(UUID classId, Long lecturerId\) \{.*?\n    \}',
    '''private void validateLecturerOwnership(List<UUID> classIds, Long lecturerId) {
        if (lecturerId == null) return; // Admin skips validation
        for (UUID classId : classIds) {
            SchoolClass schoolClass = schoolClassRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFound("Class not found with ID: " + classId));
            if (schoolClass.getLecturer() == null || !schoolClass.getLecturer().getUserId().equals(lecturerId)) {
                throw new RuntimeException("Bạn không có quyền quản lý Assignment của lớp học này.");
            }
        }
    }''',
    content,
    flags=re.DOTALL
)

# Replace classId access with classes access in validate calls
content = content.replace('validateLecturerOwnership(request.getClassId(), lecturerId);', 'validateLecturerOwnership(request.getClassIds(), lecturerId);')
content = content.replace('validateLecturerOwnership(assignment.getClassId(), lecturerId);', 'validateLecturerOwnership(assignment.getClasses().stream().map(SchoolClass::getId).collect(Collectors.toList()), lecturerId);')

# Replace createAssignment
content = content.replace('.classId(request.getClassId())', '.classes(schoolClassRepository.findAllById(request.getClassIds()))')

# Replace notification logic in createAssignment
content = re.sub(
    r'List<edufit_com_lms\.module\.lms\.entity\.ClassEnrollment> enrollments = classEnrollmentRepository\.findBySchoolClassId\(request\.getClassId\(\)\);\s*for \(edufit_com_lms\.module\.lms\.entity\.ClassEnrollment enrollment : enrollments\) \{',
    '''for (UUID classId : request.getClassIds()) {
            List<edufit_com_lms.module.lms.entity.ClassEnrollment> enrollments = classEnrollmentRepository.findBySchoolClassId(classId);
            for (edufit_com_lms.module.lms.entity.ClassEnrollment enrollment : enrollments) {''',
    content
)
# Add closing brace for the new for loop
content = content.replace('return mapToResponse(saved);', '        }\n\n        return mapToResponse(saved);')

# Replace findByClassId calls
content = content.replace('assignmentRepository.findByClassId(classId)', 'assignmentRepository.findByClasses_Id(classId)')
content = content.replace('assignmentRepository.findByClassIdIn(classIds, pageable)', 'assignmentRepository.findDistinctByClasses_IdIn(classIds, pageable)')
content = content.replace('assignmentRepository.findByClassId(classId, pageable)', 'assignmentRepository.findByClasses_Id(classId, pageable)')

# Replace mapToResponse logic
content = re.sub(
    r'String className = "Unknown Class";\s*if \(assignment\.getClassId\(\) != null\) \{\s*className = schoolClassRepository\.findById\(assignment\.getClassId\(\)\)\s*\.map\(SchoolClass::getClassName\)\s*\.orElse\("Unknown Class"\);\s*\}',
    '''List<String> classNames = assignment.getClasses().stream().map(SchoolClass::getClassName).collect(Collectors.toList());
        List<UUID> classIds = assignment.getClasses().stream().map(SchoolClass::getId).collect(Collectors.toList());''',
    content
)

content = content.replace('.classId(assignment.getClassId())', '.classIds(classIds)')
content = content.replace('.className(className)', '.classNames(classNames)')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('AssignmentServiceImpl updated.')
