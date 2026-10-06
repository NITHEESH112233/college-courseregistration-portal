package course.registration.demo.dto;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String program;
    private String semester;
    private Double cgpa;
    private Integer earnedCredits;
    private Integer registeredCredits;
    private Integer minCredits;
    private Integer maxCredits;
    private Boolean slotActive;
    private Map<String, Integer> basketCredits; // "UC": 12, "UE": 3, "PC": 48, "PE": 15
}
