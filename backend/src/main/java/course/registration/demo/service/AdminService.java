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
import java.util.stream.Collectors;

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
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new RuntimeException("Student username / registration number is required!");
        }
        String username = req.getUsername().trim().toUpperCase();
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Student with username " + username + " already exists!");
        }

        String fullName = req.getFullName() != null && !req.getFullName().isBlank()
                ? req.getFullName().trim()
                : (req.getName() != null && !req.getName().isBlank() ? req.getName().trim() : username);

        User student = User.builder()
                .username(username)
                .password(req.getPassword() != null && !req.getPassword().isBlank() ? req.getPassword().trim() : "password123")
                .fullName(fullName)
                .email(req.getEmail() != null && !req.getEmail().isBlank() ? req.getEmail().trim() : username.toLowerCase() + "@vitap.ac.in")
                .role("STUDENT")
                .program(req.getProgram() != null && !req.getProgram().isBlank() ? req.getProgram().trim() : "B.Tech Computer Science & Engineering")
                .semester(req.getSemester() != null && !req.getSemester().isBlank() ? req.getSemester().trim() : "Semester 6")
                .cgpa(req.getCgpa() != null ? req.getCgpa() : 8.5)
                .earnedCredits(req.getEarnedCredits() != null ? req.getEarnedCredits() : 120)
                .minCredits(req.getMinCredits() != null ? req.getMinCredits() : 16)
                .maxCredits(req.getMaxCredits() != null ? req.getMaxCredits() : 27)
                .slotActive(true)
                .build();

        return userRepository.save(student);
    }

    @Transactional
    public User updateStudent(Long id, CreateStudentRequest req) {
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            student.setFullName(req.getFullName().trim());
        } else if (req.getName() != null && !req.getName().isBlank()) {
            student.setFullName(req.getName().trim());
        }

        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            student.setEmail(req.getEmail().trim());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            student.setPassword(req.getPassword().trim());
        }
        if (req.getProgram() != null && !req.getProgram().isBlank()) {
            student.setProgram(req.getProgram().trim());
        }
        if (req.getSemester() != null && !req.getSemester().isBlank()) {
            student.setSemester(req.getSemester().trim());
        }
        if (req.getCgpa() != null) {
            student.setCgpa(req.getCgpa());
        }
        if (req.getEarnedCredits() != null) {
            student.setEarnedCredits(req.getEarnedCredits());
        }
        if (req.getMaxCredits() != null) {
            student.setMaxCredits(req.getMaxCredits());
        }
        if (req.getMinCredits() != null) {
            student.setMinCredits(req.getMinCredits());
        }

        return userRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public Course createCourse(CreateCourseRequest req) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new RuntimeException("Course code is required!");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new RuntimeException("Course title is required!");
        }
        String code = req.getCode().trim().toUpperCase();
        if (courseRepository.existsByCode(code)) {
            throw new RuntimeException("Course with code " + code + " already exists!");
        }

        Course course = Course.builder()
                .code(code)
                .title(req.getTitle().trim())
                .basket(CourseService.normalizeBasket(req.getBasket()))
                .credits(req.getCredits() != null ? req.getCredits() : 3.0)
                .ltpjc(req.getLtpjc() != null && !req.getLtpjc().isBlank() ? req.getLtpjc().trim() : "3 0 0 0 3.0")
                .componentType(req.getComponentType() != null && !req.getComponentType().isBlank() ? req.getComponentType().trim() : "Embedded Theory")
                .description(req.getDescription() != null && !req.getDescription().isBlank() ? req.getDescription().trim() : "Comprehensive curriculum module for academic progression.")
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
    public List<CourseSlotDTO> assignSlotsBatch(AssignCourseSlotsRequest req) {
        if (req.getCourseCode() == null || req.getCourseCode().isBlank()) {
            throw new RuntimeException("Course Code is required to assign slots.");
        }
        Course course = courseRepository.findByCode(req.getCourseCode().trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Course not found with code: " + req.getCourseCode()));

        if (req.getSlots() == null || req.getSlots().isEmpty()) {
            throw new RuntimeException("At least one slot must be provided.");
        }

        List<CourseSlot> savedList = new ArrayList<>();
        for (AssignCourseSlotsRequest.SlotItemRequest s : req.getSlots()) {
            if (s.getSlotCode() == null || s.getSlotCode().isBlank()) continue;
            String slotCode = s.getSlotCode().trim();
            String slotType = (s.getSlotType() != null && !s.getSlotType().isBlank())
                    ? s.getSlotType().trim().toUpperCase()
                    : (slotCode.toUpperCase().startsWith("L") ? "LAB" : "THEORY");
            String faculty = (s.getFacultyName() != null && !s.getFacultyName().isBlank())
                    ? s.getFacultyName().trim()
                    : "Dr. Faculty";
            String venue = (s.getVenue() != null && !s.getVenue().isBlank())
                    ? s.getVenue().trim()
                    : (slotType.equals("LAB") ? "LAB-1" : "CB-201");
            int capacity = (s.getCapacity() != null && s.getCapacity() > 0) ? s.getCapacity() : 60;

            CourseSlot slot = CourseSlot.builder()
                    .course(course)
                    .slotCode(slotCode)
                    .slotType(slotType)
                    .facultyName(faculty)
                    .venue(venue)
                    .totalSeats(capacity)
                    .availableSeats(capacity)
                    .dayTiming("Standard Schedule Timing")
                    .build();

            savedList.add(courseSlotRepository.save(slot));
        }

        return savedList.stream().map(s -> CourseSlotDTO.builder()
                .id(s.getId())
                .courseId(course.getId())
                .courseCode(course.getCode())
                .courseName(course.getTitle())
                .slotType(s.getSlotType())
                .slotCode(s.getSlotCode())
                .venue(s.getVenue())
                .facultyName(s.getFacultyName())
                .totalSeats(s.getTotalSeats())
                .availableSeats(s.getAvailableSeats())
                .dayTiming(s.getDayTiming())
                .build()).collect(Collectors.toList());
    }

    @Transactional
    public CourseSlot assignSlot(AssignSlotRequest req) {
        if (req.getCourseCode() == null || req.getCourseCode().isBlank()) {
            throw new RuntimeException("Course code is required to assign a slot!");
        }
        if (req.getSlotCode() == null || req.getSlotCode().isBlank()) {
            throw new RuntimeException("Slot code is required!");
        }
        Course course = courseRepository.findByCode(req.getCourseCode().trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Course not found: " + req.getCourseCode()));

        String slotCode = req.getSlotCode().trim();
        String slotType = req.getSlotType() != null && !req.getSlotType().isBlank()
                ? req.getSlotType().trim()
                : (slotCode.toUpperCase().startsWith("L") ? "LAB" : "THEORY");

        CourseSlot slot = CourseSlot.builder()
                .course(course)
                .slotCode(slotCode)
                .slotType(slotType)
                .facultyName(req.getFacultyName() != null && !req.getFacultyName().isBlank() ? req.getFacultyName().trim() : "Faculty Member")
                .venue(req.getVenue() != null && !req.getVenue().isBlank() ? req.getVenue().trim() : "CB-201")
                .totalSeats(req.getCapacity() != null ? req.getCapacity() : 60)
                .availableSeats(req.getCapacity() != null ? req.getCapacity() : 60)
                .dayTiming("Scheduled Timing")
                .build();

        return courseSlotRepository.save(slot);
    }

    @Transactional
    public CourseSlot reassignSlot(AssignSlotRequest req) {
        CourseSlot slot;
        if (req.getSlotId() != null) {
            slot = courseSlotRepository.findById(req.getSlotId())
                    .orElseThrow(() -> new RuntimeException("Slot not found with ID: " + req.getSlotId()));
        } else if (req.getCourseCode() != null && req.getSlotCode() != null) {
            List<CourseSlot> slots = courseSlotRepository.findByCourseCode(req.getCourseCode().trim().toUpperCase());
            slot = slots.stream()
                    .filter(s -> s.getSlotCode().equalsIgnoreCase(req.getSlotCode().trim()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Slot not found for course " + req.getCourseCode()));
        } else {
            throw new RuntimeException("Slot ID or Course Code + Slot Code required for reassignment.");
        }

        if (req.getFacultyName() != null && !req.getFacultyName().isBlank()) {
            slot.setFacultyName(req.getFacultyName().trim());
        }
        if (req.getVenue() != null && !req.getVenue().isBlank()) {
            slot.setVenue(req.getVenue().trim());
        }
        if (req.getCapacity() != null && req.getCapacity() > 0) {
            int diff = req.getCapacity() - slot.getTotalSeats();
            slot.setTotalSeats(req.getCapacity());
            slot.setAvailableSeats(Math.max(0, slot.getAvailableSeats() + diff));
        }

        return courseSlotRepository.save(slot);
    }

    public List<CourseSlot> getAllSlots() {
        return courseSlotRepository.findAll();
    }

    public List<CourseSlotDTO> getAllSlotsDTO() {
        return courseSlotRepository.findAll().stream()
                .map(s -> CourseSlotDTO.builder()
                        .id(s.getId())
                        .courseId(s.getCourse() != null ? s.getCourse().getId() : null)
                        .courseCode(s.getCourse() != null ? s.getCourse().getCode() : null)
                        .courseName(s.getCourse() != null ? s.getCourse().getTitle() : null)
                        .slotType(s.getSlotType())
                        .slotCode(s.getSlotCode())
                        .venue(s.getVenue())
                        .facultyName(s.getFacultyName())
                        .totalSeats(s.getTotalSeats())
                        .availableSeats(s.getAvailableSeats())
                        .dayTiming(s.getDayTiming())
                        .build())
                .collect(Collectors.toList());
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
            LocalDateTime dt = parseFlexibleDateTime(req.getStartDateTime());
            if (dt != null) {
                config.setStartDateTime(dt);
            }
        }
        if (req.getEndDateTime() != null && !req.getEndDateTime().isBlank()) {
            LocalDateTime dt = parseFlexibleDateTime(req.getEndDateTime());
            if (dt != null) {
                config.setEndDateTime(dt);
            }
        }

        RegistrationConfig saved = configRepository.save(config);
        return toConfigDTO(saved);
    }

    private LocalDateTime parseFlexibleDateTime(String str) {
        if (str == null || str.isBlank()) return null;
        String trimmed = str.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        );
        for (DateTimeFormatter dtf : formatters) {
            try {
                return LocalDateTime.parse(trimmed, dtf);
            } catch (Exception ignored) {}
        }
        return null;
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
