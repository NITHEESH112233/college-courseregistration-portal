package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStudentRequest {
    private String name;
    private String fullName;
    private String username;
    private String email;
    private String program;
    private String password;
    private String semester;
    private Double cgpa;
    private Integer maxCredits;
    private Integer minCredits;
    private Integer earnedCredits;

    public String getDisplayName() {
        if (fullName != null && !fullName.isBlank()) return fullName.trim();
        if (name != null && !name.isBlank()) return name.trim();
        return username != null ? username.trim() : "Student";
    }
}

