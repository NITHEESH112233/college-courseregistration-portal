package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateConfigRequest {
    private String semesterName;
    private String startDateTime;
    private String endDateTime;
    private Boolean isRegistrationOpen;
    private Integer minCredits;
    private Integer maxCredits;
    private String announcement;
}
