package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String username;
    private String fullName;
    private String role;
    private String program;
    private String semester;
    private Double cgpa;
    private Integer earnedCredits;
    private Integer maxCredits;
    private Integer registeredCredits;
}
