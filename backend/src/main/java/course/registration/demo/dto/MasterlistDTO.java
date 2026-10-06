package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterlistDTO {
    private Long id;
    private Integer rank;
    private String code;
    private String title;
    private String category;
    private Double credits;
    private SlotChoiceDTO choice1;
    private SlotChoiceDTO choice2;
    private SlotChoiceDTO choice3;
    private String status;
    private Boolean clash;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotChoiceDTO {
        private Long slotId;
        private String slot;
        private String faculty;
        private String venue;
        private Integer seats;
        private Integer maxSeats;
    }
}
