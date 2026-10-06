package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifySlotRequest {
    private String username;
    private String courseCode;
    private Long oldSlotId;
    private Long newSlotId;
    private String newSlotCode;
}
