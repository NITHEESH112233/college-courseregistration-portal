package course.registration.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private String semesterName = "Winter Semester 2024";

    @Builder.Default
    private Boolean isRegistrationOpen = true;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @Builder.Default
    private String announcement = "Your registration slot is currently active. Ensure minimum credits are met.";

    @Builder.Default
    private Integer remainingHours = 2;

    @Builder.Default
    private Integer remainingMinutes = 45;

    @Builder.Default
    private Integer remainingSeconds = 30;

    @Builder.Default
    private Integer minCredits = 16;

    @Builder.Default
    private Integer maxCredits = 27;
}
