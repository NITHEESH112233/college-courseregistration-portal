package course.registration.demo.controller;

import course.registration.demo.dto.ApiResponse;
import course.registration.demo.dto.StudentProfileDTO;
import course.registration.demo.model.User;
import course.registration.demo.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> getProfile(@PathVariable String username) {
        try {
            StudentProfileDTO profile = studentService.getStudentProfile(username);
            return ResponseEntity.ok(ApiResponse.ok(profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{username}/settings")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> updateSettings(
            @PathVariable String username,
            @RequestBody Map<String, String> payload) {
        try {
            String fullName = payload.get("fullName");
            String email = payload.get("email");
            String newPassword = payload.get("newPassword");

            StudentProfileDTO updated = studentService.updatePreferences(username, fullName, email, newPassword);
            return ResponseEntity.ok(ApiResponse.ok("Preferences updated successfully.", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
