package course.registration.demo.controller;

import course.registration.demo.dto.ApiResponse;
import course.registration.demo.dto.MasterlistDTO;
import course.registration.demo.service.MasterlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/masterlist")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MasterlistController {

    private final MasterlistService masterlistService;

    @GetMapping("/student/{username}")
    public ResponseEntity<ApiResponse<List<MasterlistDTO>>> getMasterlist(@PathVariable String username) {
        try {
            List<MasterlistDTO> list = masterlistService.getMasterlist(username);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/student/{username}/save")
    public ResponseEntity<ApiResponse<List<MasterlistDTO>>> saveMasterlist(
            @PathVariable String username,
            @RequestBody List<MasterlistDTO> items) {
        try {
            List<MasterlistDTO> list = masterlistService.saveMasterlist(username, items);
            return ResponseEntity.ok(ApiResponse.ok("Masterlist saved successfully!", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/student/{username}/execute")
    public ResponseEntity<ApiResponse<List<MasterlistDTO>>> executeFcfs(
            @PathVariable String username) {
        try {
            List<MasterlistDTO> list = masterlistService.executeFcfsRegistration(username);
            return ResponseEntity.ok(ApiResponse.ok("1-Click FCFS registration completed successfully!", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/student/{username}")
    public ResponseEntity<ApiResponse<String>> clearMasterlist(@PathVariable String username) {
        try {
            masterlistService.clearMasterlist(username);
            return ResponseEntity.ok(ApiResponse.ok("Masterlist cleared.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
