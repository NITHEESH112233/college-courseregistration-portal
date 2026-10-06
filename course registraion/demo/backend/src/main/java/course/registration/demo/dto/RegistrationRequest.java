package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {
    private String username;
    private String courseCode;
    private Long theorySlotId;
    private Long labSlotId;
    private String theorySlotCode; // fallback if ID not supplied
    private String labSlotCode;   // fallback if ID not supplied
}
