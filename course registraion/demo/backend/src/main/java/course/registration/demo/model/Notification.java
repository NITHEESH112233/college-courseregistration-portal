package course.registration.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nullable for global/broadcast notifications
    private User student;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000, nullable = false)
    private String body;

    @Column(nullable = false)
    private String category; // "registration", "academic", "system"

    @Column(nullable = false)
    private String type; // "urgent", "registration", "academic", "success", "security"

    private String timeAgo; // e.g. "10 minutes ago", "Just now"

    @Builder.Default
    private Boolean unread = true;

    private String actionText; // e.g. "Modify Registered Slots"

    private String actionLink; // e.g. "modify_slots.html"

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
