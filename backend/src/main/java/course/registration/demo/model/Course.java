package course.registration.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. "SWE3004", "CSE1001"

    @Column(nullable = false)
    private String title; // e.g. "Front End Design and Testing"

    @Column(nullable = false)
    private String basket; // "PC" (Program Core), "PE" (Program Elective), "UC" (University Core), "UE" (University Elective)

    @Column(nullable = false)
    private Double credits; // e.g. 4.0, 3.0, 2.0

    private String ltpjc; // e.g. "3 0 2 0 4.0"

    private String componentType; // "Embedded Theory / Embedded Lab", "Theory Only", "Lab Only"

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseSlot> slots = new ArrayList<>();
}
