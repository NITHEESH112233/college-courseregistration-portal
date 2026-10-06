package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationConfigDTO {
    private Long id;
    private String semesterName;
    private String startDateTime;
    private String endDateTime;
    private String formattedStartDate;
    private String formattedEndDate;
    private Boolean isRegistrationOpen;
    private String status;
    private String statusMessage;
    private Long totalRemainingSeconds;
    private Integer remainingHours;
    private Integer remainingMinutes;
    private Integer remainingSeconds;
    private Integer minCredits;
    private Integer maxCredits;
    private String announcement;
}
