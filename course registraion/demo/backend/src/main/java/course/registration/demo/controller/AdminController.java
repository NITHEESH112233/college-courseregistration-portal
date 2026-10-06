package course.registration.demo.controller;

import course.registration.demo.dto.*;
import course.registration.demo.model.Course;
import course.registration.demo.model.CourseSlot;
import course.registration.demo.model.RegistrationConfig;
import course.registration.demo.model.User;
import course.registration.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDTO>> getStats() {
        try {
            AdminStatsDTO stats = adminService.getStats();
            return ResponseEntity.ok(ApiResponse.ok(stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<User>>> getAllStudents() {
        try {
            List<User> list = adminService.getAllStudents();
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<User>> createStudent(@RequestBody CreateStudentRequest request) {
        try {
            User u = adminService.createStudent(request);
            return ResponseEntity.ok(ApiResponse.ok("Student credentials created successfully!", u));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<User>> updateStudent(@PathVariable Long id, @RequestBody CreateStudentRequest request) {
        try {
            User u = adminService.updateStudent(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Student account updated successfully!", u));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<String>> deleteStudent(@PathVariable Long id) {
        try {
            adminService.deleteStudent(id);
            return ResponseEntity.ok(ApiResponse.ok("Student account deleted successfully from database.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody CreateCourseRequest request) {
        try {
            Course c = adminService.createCourse(request);
            return ResponseEntity.ok(ApiResponse.ok("Course published to registration catalog successfully!", c));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCourse(@PathVariable Long id) {
        try {
            adminService.deleteCourse(id);
            return ResponseEntity.ok(ApiResponse.ok("Course deleted successfully from catalog.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/courses/code/{code}")
    public ResponseEntity<ApiResponse<String>> deleteCourseByCode(@PathVariable String code) {
        try {
            adminService.deleteCourseByCode(code);
            return ResponseEntity.ok(ApiResponse.ok("Course " + code + " deleted successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<CourseSlot>>> getAllSlots() {
        try {
            List<CourseSlot> list = adminService.getAllSlots();
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<CourseSlot>> assignSlot(@RequestBody AssignSlotRequest request) {
        try {
            CourseSlot s = adminService.assignSlot(request);
            return ResponseEntity.ok(ApiResponse.ok("Slot assigned to course successfully!", s));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSlot(@PathVariable Long id) {
        try {
            adminService.deleteSlot(id);
            return ResponseEntity.ok(ApiResponse.ok("Slot deleted successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<RegistrationConfigDTO>> getConfig() {
        try {
            RegistrationConfigDTO cfg = adminService.getConfigDTO();
            return ResponseEntity.ok(ApiResponse.ok("Registration configuration retrieved successfully.", cfg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/config")
    public ResponseEntity<ApiResponse<RegistrationConfigDTO>> updateConfig(@RequestBody UpdateConfigRequest req) {
        try {
            RegistrationConfigDTO cfg = adminService.updateConfig(req);
            return ResponseEntity.ok(ApiResponse.ok("Registration configuration updated successfully.", cfg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/controls/extend")
    public ResponseEntity<ApiResponse<RegistrationConfigDTO>> extendWindow(@RequestBody Map<String, Integer> payload) {
        try {
            int minutes = payload.getOrDefault("minutes", 60);
            RegistrationConfigDTO cfg = adminService.extendWindow(minutes);
            return ResponseEntity.ok(ApiResponse.ok("Registration window extended by " + minutes + " minutes.", cfg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/controls/toggle-pause")
    public ResponseEntity<ApiResponse<RegistrationConfigDTO>> togglePause() {
        try {
            RegistrationConfigDTO cfg = adminService.togglePause();
            String msg = "PAUSED".equalsIgnoreCase(cfg.getStatus()) ? "Registration slot paused." : "Registration slot resumed.";
            return ResponseEntity.ok(ApiResponse.ok(msg, cfg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
