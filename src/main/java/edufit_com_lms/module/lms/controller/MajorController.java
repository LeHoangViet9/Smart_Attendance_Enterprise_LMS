package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.entity.Major;
import edufit_com_lms.module.lms.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/majors")
@RequiredArgsConstructor
public class MajorController {

    private final MajorRepository majorRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Major>>> getAllMajors() {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách chuyên ngành thành công",
                null,
                majorRepository.findAll(),
                HttpStatus.OK), HttpStatus.OK);
    }
}
