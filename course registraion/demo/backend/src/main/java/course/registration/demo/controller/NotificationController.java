package course.registration.demo.controller;

import course.registration.demo.dto.ApiResponse;
import course.registration.demo.model.Notification;
import course.registration.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @RequestParam(required = false) String category) {
        try {
            List<Notification> list = notificationService.getAllNotifications(category);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> toggleRead(@PathVariable Long id) {
        try {
            Notification n = notificationService.toggleReadStatus(id);
            return ResponseEntity.ok(ApiResponse.ok(n));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok(ApiResponse.ok("All marked as read.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(ApiResponse.ok("Notification dismissed.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse<String>> clearAll() {
        try {
            notificationService.clearAll();
            return ResponseEntity.ok(ApiResponse.ok("All notifications cleared.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<Notification>> simulateAlert() {
        try {
            Notification n = notificationService.simulateAlert();
            return ResponseEntity.ok(ApiResponse.ok("Live alert triggered!", n));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
