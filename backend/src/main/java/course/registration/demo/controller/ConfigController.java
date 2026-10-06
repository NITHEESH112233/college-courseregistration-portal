package course.registration.demo.controller;

import course.registration.demo.dto.ApiResponse;
import course.registration.demo.dto.RegistrationConfigDTO;
import course.registration.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConfigController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<RegistrationConfigDTO>> getRegistrationConfig() {
        try {
            RegistrationConfigDTO cfg = adminService.getConfigDTO();
            return ResponseEntity.ok(ApiResponse.ok("Registration configuration retrieved successfully.", cfg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
