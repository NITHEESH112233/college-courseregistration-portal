package course.registration.demo.service;

import course.registration.demo.model.Notification;
import course.registration.demo.model.User;
import course.registration.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getAllNotifications(String category) {
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category.trim())) {
            return notificationRepository.findByCategoryOrderByCreatedAtDesc(category.trim());
        }
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public Notification toggleReadStatus(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
        n.setUnread(!n.getUnread());
        return notificationRepository.save(n);
    }

    public void markAllAsRead() {
        List<Notification> list = notificationRepository.findAll();
        for (Notification n : list) {
            n.setUnread(false);
        }
        notificationRepository.saveAll(list);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public void clearAll() {
        notificationRepository.deleteAll();
    }

    public Notification createNotification(User student, String title, String category, String type, String body, String actionText, String actionLink) {
        Notification newAlert = Notification.builder()
                .student(student)
                .title(title)
                .category(category != null ? category : "registration")
                .type(type != null ? type : "urgent")
                .unread(true)
                .timeAgo("Just now")
                .body(body)
                .actionText(actionText != null ? actionText : "View Details")
                .actionLink(actionLink != null ? actionLink : "quick_register.html")
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(newAlert);
    }

    public Notification simulateAlert() {
        return createNotification(
                null,
                "Slot Opening Alert: Lab Slots Available",
                "registration",
                "urgent",
                "Slot L31+L32 for Advanced Web Development has opened 15 additional workstations in Tech Tower.",
                "Register Now",
                "slot_selection.html"
        );
    }
}

