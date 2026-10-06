package course.registration.demo.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private Long id;
    private String code;
    private String title;
    private String basket;
    private Double credits;
    private String ltpjc;
    private String componentType;
    private String description;
    private boolean isRegistered;
    private List<CourseSlotDTO> theorySlots;
    private List<CourseSlotDTO> labSlots;
}
