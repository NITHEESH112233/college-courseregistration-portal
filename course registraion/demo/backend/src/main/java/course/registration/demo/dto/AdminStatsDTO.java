package course.registration.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsDTO {
    private long totalStudents;
    private long activeCourses;
    private long slotsAllocated;
    private int seatCapacityPercent;
    private boolean registrationActive;
    private String semesterName;
    private int remainingHours;
    private int remainingMinutes;
}
