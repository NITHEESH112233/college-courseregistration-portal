package course.registration.demo.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseRequest {
    private String code;
    private String title;
    private String basket;
    private Double credits;
    private String ltpjc;
    private String componentType;
    private String description;
    private List<SlotRequest> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotRequest {
        private String slotCode;
        private String slotType; // "THEORY" or "LAB"
        private String facultyName;
        private String venue;
        private Integer capacity;
    }
}
