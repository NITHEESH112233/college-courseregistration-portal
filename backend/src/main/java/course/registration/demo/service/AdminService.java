package course.registration.demo.service;

import course.registration.demo.dto.*;
import course.registration.demo.model.Course;
import course.registration.demo.model.CourseSlot;
import course.registration.demo.model.RegistrationConfig;
import course.registration.demo.model.User;
import course.registration.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseSlotRepository courseSlotRepository;
    private final RegistrationRepository registrationRepository;
    private final MasterlistRepository masterlistRepository;
    private final RegistrationConfigRepository configRepository;

    public AdminStatsDTO getStats() {
        long totalStudents = userRepository.count();
        long activeCourses = courseRepository.count();
        long slotsAllocated = registrationRepository.count();

        List<CourseSlot> allSlots = courseSlotRepository.findAll();
        int totalCapacity = allSlots.stream().mapToInt(CourseSlot::getTotalSeats).sum();
        int availableSeats = allSlots.stream().mapToInt(CourseSlot::getAvailableSeats).sum();
        int seatCapacityPercent = totalCapacity > 0 ? (int) Math.round(((double) (totalCapacity - availableSeats) / totalCapacity) * 100) : 84;

        RegistrationConfigDTO configDTO = getConfigDTO();

        return AdminStatsDTO.builder()
                .totalStudents(totalStudents)
                .activeCourses(activeCourses)
                .slotsAllocated(slotsAllocated)
                .seatCapacityPercent(seatCapacityPercent > 0 ? seatCapacityPercent : 84)
                .registrationActive("ACTIVE".equalsIgnoreCase(configDTO.getStatus()))
                .semesterName(configDTO.getSemesterName())
                .remainingHours(configDTO.getRemainingHours())
                .remainingMinutes(configDTO.getRemainingMinutes())
                .build();
    }

    public List<User> getAllStudents() {
        return userRepository.findAll();
    }

    @Transactional
    public User createStudent(CreateStudentRequest req) {
        if (userRepository.existsByUsername(req.getUsername().trim())) {
            throw new RuntimeException("Student with username " + req.getUsername() + " already exists!");
        }

        User student = User.builder()
                .username(req.getUsername().trim())
                .password(req.getPassword() != null && !req.getPassword().isBlank() ? req.getPassword() : "password123")
                .fullName(req.getFullName().trim())
                .email(req.getEmail() != null && !req.getEmail().isBlank() ? req.getEmail().trim() : req.getUsername() + "@vitap.ac.in")
                .role("STUDENT")
                .program(req.getProgram() != null ? req.getProgram() : "B.Tech Computer Science")
                .semester(req.getSemester() != null ? req.getSemester() : "Semester 6")
                .cgpa(req.getCgpa() != null ? req.getCgpa() : 8.5)
                .earnedCredits(req.getEarnedCredits() != null ? req.getEarnedCredits() : 120)
                .minCredits(16)
                .maxCredits(27)
                .slotActive(true)
                .build();

        return userRepository.save(student);
    }

    @Transactional
    public User updateStudent(Long id, CreateStudentRequest req) {
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));

        student.setFullName(req.getFullName().trim());
        if (req.getEmail() != null) student.setEmail(req.getEmail().trim());
        if (req.getPassword() != null && !req.getPassword().isBlank()) student.setPassword(req.getPassword());
        if (req.getProgram() != null) student.setProgram(req.getProgram());
        if (req.getSemester() != null) student.setSemester(req.getSemester());
        if (req.getCgpa() != null) student.setCgpa(req.getCgpa());
        if (req.getEarnedCredits() != null) student.setEarnedCredits(req.getEarnedCredits());

        return userRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public Course createCourse(CreateCourseRequest req) {
        if (courseRepository.existsByCode(req.getCode().trim().toUpperCase())) {
            throw new RuntimeException("Course with code " + req.getCode() + " already exists!");
        }

        Course course = Course.builder()
                .code(req.getCode().trim().toUpperCase())
                .title(req.getTitle().trim())
                .basket(req.getBasket() != null ? req.getBasket().trim().toUpperCase() : "PC")
                .credits(req.getCredits() != null ? req.getCredits() : 3.0)
                .ltpjc(req.getLtpjc() != null ? req.getLtpjc() : "3 0 0 0 3.0")
                .componentType(req.getComponentType() != null ? req.getComponentType() : "Embedded Theory")
                .description(req.getDescription() != null ? req.getDescription().trim() : "Comprehensive curriculum module for academic progression.")
                .build();

        Course savedCourse = courseRepository.save(course);

        if (req.getSlots() != null && !req.getSlots().isEmpty()) {
            for (CreateCourseRequest.SlotRequest s : req.getSlots()) {
                CourseSlot slot = CourseSlot.builder()
                        .course(savedCourse)
                        .slotCode(s.getSlotCode() != null ? s.getSlotCode().trim() : "A1+TA1")
                        .slotType(s.getSlotType() != null ? s.getSlotType() : (s.getSlotCode() != null && s.getSlotCode().toUpperCase().startsWith("L") ? "LAB" : "THEORY"))
                        .facultyName(s.getFacultyName() != null ? s.getFacultyName().trim() : "Dr. Course Faculty")
                        .venue(s.getVenue() != null ? s.getVenue().trim() : "CB-204")
                        .totalSeats(s.getCapacity() != null ? s.getCapacity() : 60)
                        .availableSeats(s.getCapacity() != null ? s.getCapacity() : 60)
                        .dayTiming("Standard Schedule Timing")
                        .build();
                courseSlotRepository.save(slot);
            }
        } else {
            CourseSlot primaryTheorySlot = CourseSlot.builder()
                    .course(savedCourse)
                    .slotCode("A1+TA1")
                    .slotType("THEORY")
                    .facultyName("Dr. Course Faculty")
                    .venue("CB-204")
                    .totalSeats(60)
                    .availableSeats(60)
                    .dayTiming("Standard Schedule Timing")
                    .build();
            courseSlotRepository.save(primaryTheorySlot);

            if (savedCourse.getComponentType().contains("Lab")) {
                CourseSlot labSlot = CourseSlot.builder()
                        .course(savedCourse)
                        .slotCode("L11+L12")
                        .slotType("LAB")
                        .facultyName("Dr. Course Faculty")
                        .venue("LAB-2")
                        .totalSeats(60)
                        .availableSeats(60)
                        .dayTiming("Thu 14:00 - 15:50")
                        .build();
                courseSlotRepository.save(labSlot);
            }
        }

        return savedCourse;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    public CourseSlot assignSlot(AssignSlotRequest req) {
        Course course = courseRepository.findByCode(req.getCourseCode().trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Course not found: " + req.getCourseCode()));

        CourseSlot slot = CourseSlot.builder()
                .course(course)
                .slotCode(req.getSlotCode().trim())
                .slotType(req.getSlotType() != null ? req.getSlotType() : (req.getSlotCode().toUpperCase().startsWith("L") ? "LAB" : "THEORY"))
                .facultyName(req.getFacultyName().trim())
                .venue(req.getVenue().trim())
                .totalSeats(req.getCapacity() != null ? req.getCapacity() : 60)
                .availableSeats(req.getCapacity() != null ? req.getCapacity() : 60)
                .dayTiming("Scheduled Timing")
                .build();

        return courseSlotRepository.save(slot);
    }

    public List<CourseSlot> getAllSlots() {
        return courseSlotRepository.findAll();
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        courseSlotRepository.deleteById(slotId);
    }

    public RegistrationConfigDTO getConfigDTO() {
        RegistrationConfig config = configRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    RegistrationConfig initial = RegistrationConfig.builder()
                            .semesterName("Winter Semester 2024")
                            .startDateTime(LocalDateTime.now().minusHours(1))
                            .endDateTime(LocalDateTime.now().plusHours(3))
                            .isRegistrationOpen(true)
                            .announcement("Your registration slot is currently active. Ensure minimum credits are met.")
                            .minCredits(16)
                            .maxCredits(27)
                            .build();
                    return configRepository.save(initial);
                });

        return toConfigDTO(config);
    }

    public RegistrationConfigDTO toConfigDTO(RegistrationConfig config) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = config.getStartDateTime() != null ? config.getStartDateTime() : now.minusHours(1);
        LocalDateTime end = config.getEndDateTime() != null ? config.getEndDateTime() : now.plusHours(3);

        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        String status;
        String statusMessage;
        long totalSecs;

        if (Boolean.FALSE.equals(config.getIsRegistrationOpen())) {
            status = "PAUSED";
            statusMessage = "Registration is currently paused by the Administrator.";
            totalSecs = 0;
        } else if (now.isBefore(start)) {
            status = "UPCOMING";
            statusMessage = "Registration window will open on " + start.format(displayFormatter) + ".";
            totalSecs = Duration.between(now, start).getSeconds();
        } else if (now.isAfter(end)) {
            status = "CLOSED";
            statusMessage = "Registration window has concluded. Changes can no longer be made.";
            totalSecs = 0;
        } else {
            status = "ACTIVE";
            statusMessage = config.getAnnouncement() != null && !config.getAnnouncement().isBlank()
                    ? config.getAnnouncement()
                    : "Your registration slot is currently active. Ensure minimum credits are met.";
            totalSecs = Duration.between(now, end).getSeconds();
        }

        if (totalSecs < 0) totalSecs = 0;

        int remHours = (int) (totalSecs / 3600);
        int remMins = (int) ((totalSecs % 3600) / 60);
        int remSecs = (int) (totalSecs % 60);

        return RegistrationConfigDTO.builder()
                .id(config.getId())
                .semesterName(config.getSemesterName())
                .startDateTime(start.format(isoFormatter))
                .endDateTime(end.format(isoFormatter))
                .formattedStartDate(start.format(displayFormatter))
                .formattedEndDate(end.format(displayFormatter))
                .isRegistrationOpen(config.getIsRegistrationOpen())
                .status(status)
                .statusMessage(statusMessage)
                .totalRemainingSeconds(totalSecs)
                .remainingHours(remHours)
                .remainingMinutes(remMins)
                .remainingSeconds(remSecs)
                .minCredits(config.getMinCredits())
                .maxCredits(config.getMaxCredits())
                .announcement(config.getAnnouncement())
                .build();
    }

    @Transactional
    public RegistrationConfigDTO updateConfig(UpdateConfigRequest req) {
        RegistrationConfig config = configRepository.findAll().stream().findFirst()
                .orElse(RegistrationConfig.builder().build());

        if (req.getSemesterName() != null && !req.getSemesterName().isBlank()) {
            config.setSemesterName(req.getSemesterName().trim());
        }
        if (req.getAnnouncement() != null) {
            config.setAnnouncement(req.getAnnouncement().trim());
        }
        if (req.getIsRegistrationOpen() != null) {
            config.setIsRegistrationOpen(req.getIsRegistrationOpen());
        }
        if (req.getMinCredits() != null) {
            config.setMinCredits(req.getMinCredits());
        }
        if (req.getMaxCredits() != null) {
            config.setMaxCredits(req.getMaxCredits());
        }

        if (req.getStartDateTime() != null && !req.getStartDateTime().isBlank()) {
            try {
                config.setStartDateTime(LocalDateTime.parse(req.getStartDateTime().trim()));
            } catch (Exception ignored) {}
        }
        if (req.getEndDateTime() != null && !req.getEndDateTime().isBlank()) {
            try {
                config.setEndDateTime(LocalDateTime.parse(req.getEndDateTime().trim()));
            } catch (Exception ignored) {}
        }

        RegistrationConfig saved = configRepository.save(config);
        return toConfigDTO(saved);
    }

    @Transactional
    public RegistrationConfigDTO extendWindow(int minutes) {
        RegistrationConfig config = configRepository.findAll().stream().findFirst()
                .orElse(RegistrationConfig.builder().build());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseEnd = config.getEndDateTime() != null && config.getEndDateTime().isAfter(now)
                ? config.getEndDateTime()
                : now;

        config.setEndDateTime(baseEnd.plusMinutes(minutes));
        config.setIsRegistrationOpen(true);
        RegistrationConfig saved = configRepository.save(config);
        return toConfigDTO(saved);
    }

    @Transactional
    public RegistrationConfigDTO togglePause() {
        RegistrationConfig config = configRepository.findAll().stream().findFirst()
                .orElse(RegistrationConfig.builder().build());

        boolean currentOpen = config.getIsRegistrationOpen() != null ? config.getIsRegistrationOpen() : true;
        config.setIsRegistrationOpen(!currentOpen);
        RegistrationConfig saved = configRepository.save(config);
        return toConfigDTO(saved);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
        deleteCourseEntity(course);
    }

    @Transactional
    public void deleteCourseByCode(String code) {
        Course course = courseRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Course not found with code: " + code));
        deleteCourseEntity(course);
    }

    private void deleteCourseEntity(Course course) {
        masterlistRepository.deleteByCourseId(course.getId());
        registrationRepository.deleteAll(registrationRepository.findByCourseId(course.getId()));
        courseSlotRepository.deleteAll(courseSlotRepository.findByCourseId(course.getId()));
        courseRepository.delete(course);
    }
}
