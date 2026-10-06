package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponseDTO {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private String basket;
    private String componentType; // "Theory" or "Lab"
    private String slotCode;
    private String facultyName;
    private String venue;
    private Double credits;
    private String registeredAt;
}
