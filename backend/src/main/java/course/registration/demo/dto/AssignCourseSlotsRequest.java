package course.registration.demo.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignCourseSlotsRequest {
    private String courseCode;
    private List<SlotItemRequest> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotItemRequest {
        private String slotCode;
        private String slotType; // "THEORY" or "LAB"
        private String facultyName;
        private String venue;
        private Integer capacity;
    }
}
