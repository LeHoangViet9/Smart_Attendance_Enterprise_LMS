package edufit_com_lms.module.auth.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/seed-data")
@RequiredArgsConstructor
public class DataSeederController {

    private final DataSeederService dataSeederService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> runDataSeeder() {
        dataSeederService.seedData();
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Data seeding completed successfully",
                null,
                "Seeded Profiles, Majors, Classes and Enrollments.",
                HttpStatus.OK), HttpStatus.OK);
    }
}
