package course.registration.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "masterlist_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private Integer rankNumber; // 1, 2, 3, etc.

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "choice1_slot_id")
    private CourseSlot choice1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "choice2_slot_id")
    private CourseSlot choice2;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "choice3_slot_id")
    private CourseSlot choice3;

    @Builder.Default
    private String status = "Ready"; // "Ready", "Allocated", "Clash"
}
