package edufit_com_lms.module.auth.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.dto.request.AdminRegisterRequest;
import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Get all users successfully",
                null,
                userService.findAll(keyword, role, isActive, pageable),
                HttpStatus.OK), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id,
            @RequestBody AdminRegisterRequest adminRegisterRequest) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Update user successfully",
                null,
                userService.updateUser(id, adminRegisterRequest),
                HttpStatus.OK), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Delete user successfully",
                null,
                null,
                HttpStatus.OK), HttpStatus.OK);
    }
}
