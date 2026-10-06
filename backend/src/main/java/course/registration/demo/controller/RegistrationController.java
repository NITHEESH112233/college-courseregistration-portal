package course.registration.demo.controller;

import course.registration.demo.dto.ApiResponse;
import course.registration.demo.dto.ModifySlotRequest;
import course.registration.demo.dto.RegistrationRequest;
import course.registration.demo.dto.RegistrationResponseDTO;
import course.registration.demo.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/registrations", "/api/registration"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping({"/{username}", "/student/{username}"})
    public ResponseEntity<ApiResponse<List<RegistrationResponseDTO>>> getStudentRegistrations(
            @PathVariable String username) {
        try {
            List<RegistrationResponseDTO> list = registrationService.getStudentRegistrations(username);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<RegistrationResponseDTO>>> registerCourse(
            @RequestBody RegistrationRequest request) {
        try {
            List<RegistrationResponseDTO> list = registrationService.registerCourse(request);
            return ResponseEntity.ok(ApiResponse.ok("Course successfully registered!", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @RequestMapping(value = {"/modify", "/modify-slot"}, method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<List<RegistrationResponseDTO>>> modifySlot(
            @RequestBody ModifySlotRequest request) {
        try {
            List<RegistrationResponseDTO> list = registrationService.modifySlot(request);
            return ResponseEntity.ok(ApiResponse.ok("Course slot modified successfully!", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping({"/{id}", "/drop/{id}"})
    public ResponseEntity<ApiResponse<String>> deleteRegistration(@PathVariable Long id) {
        try {
            registrationService.deleteRegistration(id);
            return ResponseEntity.ok(ApiResponse.ok("Slot dropped successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/student/{username}/course/{courseCode}")
    public ResponseEntity<ApiResponse<String>> dropCourse(
            @PathVariable String username,
            @PathVariable String courseCode) {
        try {
            registrationService.dropCourseForStudent(username, courseCode);
            return ResponseEntity.ok(ApiResponse.ok("Course " + courseCode + " dropped successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<String>> batchDropCourses(
            @RequestBody Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            @SuppressWarnings("unchecked")
            List<String> courseCodes = (List<String>) payload.get("courseCodes");

            if (username != null && courseCodes != null) {
                for (String code : courseCodes) {
                    registrationService.dropCourseForStudent(username, code);
                }
            }
            return ResponseEntity.ok(ApiResponse.ok("Selected courses dropped successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
