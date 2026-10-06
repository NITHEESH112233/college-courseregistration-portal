package course.registration.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    @Column(nullable = false)
    private String slotType; // "THEORY" or "LAB"

    @Column(nullable = false)
    private String slotCode; // e.g. "A1+TA1", "E2+TE2", "L11+L12"

    @Column(nullable = false)
    private String venue; // e.g. "CB-524", "AB-101", "LAB-4"

    @Column(nullable = false)
    private String facultyName; // e.g. "Dr. Ananya Sharma", "N MD JUBAIR BASHA"

    @Builder.Default
    private Integer totalSeats = 70;

    @Builder.Default
    private Integer availableSeats = 65;

    private String dayTiming; // e.g. "Mon 10:00 - 11:50", "Thu 14:00 - 15:50"
}
