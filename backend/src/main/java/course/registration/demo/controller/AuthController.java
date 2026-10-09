package course.registration.demo.controller;

import course.registration.demo.dto.ApiResponse;
import course.registration.demo.dto.LoginRequest;
import course.registration.demo.dto.LoginResponse;
import course.registration.demo.dto.StudentProfileDTO;
import course.registration.demo.service.AuthService;
import course.registration.demo.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final StudentService studentService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/profile/{regNo}")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> getProfile(
            @PathVariable String regNo) {

        try {
            StudentProfileDTO profile = studentService.getStudentProfile(regNo);
            return ResponseEntity.ok(ApiResponse.ok(profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}