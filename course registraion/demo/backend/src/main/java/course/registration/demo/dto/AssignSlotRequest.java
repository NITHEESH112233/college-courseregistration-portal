package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignSlotRequest {
    private Long slotId;
    private String courseCode;
    private String slotCode;
    private String slotType; // "THEORY" or "LAB"
    private String facultyName;
    private String venue;
    private Integer capacity;
}
