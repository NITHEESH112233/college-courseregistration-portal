package course.registration.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // e.g. "21BCE0000" or "admin"

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role; // "STUDENT" or "ADMIN"

    private String program; // e.g. "B.Tech Computer Science & Engineering"

    private String semester; // e.g. "Semester 6"

    private Double cgpa; // e.g. 8.95

    @Builder.Default
    private Integer earnedCredits = 120;

    @Builder.Default
    private Integer maxCredits = 27;

    @Builder.Default
    private Integer minCredits = 16;

    @Builder.Default
    private Boolean slotActive = true;
}
