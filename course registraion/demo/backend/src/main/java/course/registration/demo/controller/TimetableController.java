package course.registration.demo.controller;

import course.registration.demo.dto.ApiResponse;
import course.registration.demo.dto.RegistrationResponseDTO;
import course.registration.demo.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TimetableController {

    private final RegistrationService registrationService;

    @GetMapping("/{regNo}")
    public ResponseEntity<ApiResponse<List<RegistrationResponseDTO>>> getTimetable(@PathVariable String regNo) {
        try {
            List<RegistrationResponseDTO> list = registrationService.getStudentRegistrations(regNo);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
