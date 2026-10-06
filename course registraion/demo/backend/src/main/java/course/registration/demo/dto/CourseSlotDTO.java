package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSlotDTO {
    private Long id;
    private String slotType; // "THEORY" or "LAB"
    private String slotCode; // "E2+TE2", "L11+L12"
    private String venue;
    private String facultyName;
    private Integer totalSeats;
    private Integer availableSeats;
    private String dayTiming;
}
