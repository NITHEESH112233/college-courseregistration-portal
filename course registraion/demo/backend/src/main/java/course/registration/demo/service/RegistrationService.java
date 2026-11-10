package course.registration.demo.service;

import course.registration.demo.dto.ModifySlotRequest;
import course.registration.demo.dto.RegistrationConfigDTO;
import course.registration.demo.dto.RegistrationRequest;
import course.registration.demo.dto.RegistrationResponseDTO;
import course.registration.demo.model.Course;
import course.registration.demo.model.CourseSlot;
import course.registration.demo.model.Registration;
import course.registration.demo.model.User;
import course.registration.demo.repository.CourseRepository;
import course.registration.demo.repository.CourseSlotRepository;
import course.registration.demo.repository.RegistrationRepository;
import course.registration.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseSlotRepository courseSlotRepository;
    private final AdminService adminService;

    public void validateRegistrationWindow(User student) {
        if (student != null && Boolean.FALSE.equals(student.getSlotActive())) {
            throw new RuntimeException("Your student registration slot is currently inactive. Please contact the Administrator.");
        }

        RegistrationConfigDTO config = adminService.getConfigDTO();
        if (config == null || !"ACTIVE".equalsIgnoreCase(config.getStatus())) {
            String status = config != null ? config.getStatus() : "INACTIVE";
            String reason;
            if ("UPCOMING".equalsIgnoreCase(status)) {
                reason = "Registration is permitted only in your allotted time slot. The registration window will open on " + config.getFormattedStartDate() + ".";
            } else if ("CLOSED".equalsIgnoreCase(status)) {
                reason = "Registration is closed. The allotted registration time slot ended on " + config.getFormattedEndDate() + ".";
            } else if ("PAUSED".equalsIgnoreCase(status)) {
                reason = "Course registration is currently paused by the Administrator.";
            } else {
                reason = "Course registration is not currently active for your allotted time slot. Registrations are restricted to scheduled hours.";
            }
            throw new RuntimeException(reason);
        }
    }

    public List<RegistrationResponseDTO> getStudentRegistrations(String username) {
        List<Registration> list = registrationRepository.findByStudentUsernameAndStatus(username, "REGISTERED");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        return list.stream().map(r -> {
            boolean isLab = r.getCourseSlot() != null && "LAB".equalsIgnoreCase(r.getCourseSlot().getSlotType());
            double courseCredits = (r.getCourse() != null && r.getCourse().getCredits() != null) ? r.getCourse().getCredits() : 3.0;
            boolean hasLabComp = r.getCourse() != null && r.getCourse().getComponentType() != null && r.getCourse().getComponentType().toLowerCase().contains("lab");
            double credits = isLab ? 1.0 : (hasLabComp && courseCredits > 1.0 ? courseCredits - 1.0 : courseCredits);

            return RegistrationResponseDTO.builder()
                    .id(r.getId())
                    .courseId(r.getCourse() != null ? r.getCourse().getId() : null)
                    .courseCode(r.getCourse() != null ? r.getCourse().getCode() : "")
                    .courseTitle(r.getCourse() != null ? r.getCourse().getTitle() : "")
                    .basket(CourseService.normalizeBasket(r.getCourse() != null ? r.getCourse().getBasket() : ""))
                    .componentType(isLab ? "Lab" : "Theory")
                    .slotId(r.getCourseSlot() != null ? r.getCourseSlot().getId() : null)
                    .slotCode(r.getCourseSlot() != null ? r.getCourseSlot().getSlotCode() : "")
                    .facultyName(r.getCourseSlot() != null ? r.getCourseSlot().getFacultyName() : "")
                    .venue(r.getCourseSlot() != null ? r.getCourseSlot().getVenue() : "")
                    .credits(credits)
                    .registeredAt(r.getRegisteredAt() != null ? r.getRegisteredAt().format(dtf) : "Recent")
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<RegistrationResponseDTO> registerCourse(RegistrationRequest request) {
        User student = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Student not found: " + request.getUsername()));

        validateRegistrationWindow(student);

        Course course = courseRepository.findByCode(request.getCourseCode().trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Course not found: " + request.getCourseCode()));

        boolean alreadyReg = registrationRepository.existsByStudentUsernameAndCourseCodeAndStatus(
                student.getUsername(), course.getCode(), "REGISTERED");
        if (alreadyReg) {
            throw new RuntimeException("Student is already registered for course: " + course.getCode());
        }

        List<Registration> createdRegistrations = new ArrayList<>();

        // 1. Handle Theory Slot
        CourseSlot theorySlot = null;
        if (request.getTheorySlotId() != null) {
            theorySlot = courseSlotRepository.findById(request.getTheorySlotId())
                    .orElse(null);
        }
        if (theorySlot == null && request.getTheorySlotCode() != null && !request.getTheorySlotCode().isBlank()) {
            List<CourseSlot> slots = courseSlotRepository.findByCourseCodeAndSlotType(course.getCode(), "THEORY");
            final String reqCode = request.getTheorySlotCode().replaceAll("\\s+", "").toUpperCase();
            theorySlot = slots.stream()
                    .filter(s -> s.getSlotCode() != null && s.getSlotCode().replaceAll("\\s+", "").equalsIgnoreCase(reqCode))
                    .findFirst()
                    .orElse(null);
        }

        if (theorySlot != null) {
            int avail = theorySlot.getAvailableSeats() != null ? theorySlot.getAvailableSeats() : (theorySlot.getTotalSeats() != null ? theorySlot.getTotalSeats() : 70);
            if (avail <= 0) {
                throw new RuntimeException("No seats available in theory slot: " + theorySlot.getSlotCode());
            }
            theorySlot.setAvailableSeats(avail - 1);
            courseSlotRepository.save(theorySlot);

            Registration reg = Registration.builder()
                    .student(student)
                    .course(course)
                    .courseSlot(theorySlot)
                    .registeredAt(LocalDateTime.now())
                    .status("REGISTERED")
                    .build();
            createdRegistrations.add(registrationRepository.save(reg));
        }

        // 2. Handle Lab Slot if present
        CourseSlot labSlot = null;
        if (request.getLabSlotId() != null) {
            labSlot = courseSlotRepository.findById(request.getLabSlotId())
                    .orElse(null);
        }
        if (labSlot == null && request.getLabSlotCode() != null && !request.getLabSlotCode().isBlank()) {
            List<CourseSlot> slots = courseSlotRepository.findByCourseCodeAndSlotType(course.getCode(), "LAB");
            final String reqCode = request.getLabSlotCode().replaceAll("\\s+", "").toUpperCase();
            labSlot = slots.stream()
                    .filter(s -> s.getSlotCode() != null && s.getSlotCode().replaceAll("\\s+", "").equalsIgnoreCase(reqCode))
                    .findFirst()
                    .orElse(null);
        }

        if (labSlot != null) {
            int avail = labSlot.getAvailableSeats() != null ? labSlot.getAvailableSeats() : (labSlot.getTotalSeats() != null ? labSlot.getTotalSeats() : 70);
            if (avail <= 0) {
                throw new RuntimeException("No seats available in lab slot: " + labSlot.getSlotCode());
            }
            labSlot.setAvailableSeats(avail - 1);
            courseSlotRepository.save(labSlot);

            Registration reg = Registration.builder()
                    .student(student)
                    .course(course)
                    .courseSlot(labSlot)
                    .registeredAt(LocalDateTime.now())
                    .status("REGISTERED")
                    .build();
            createdRegistrations.add(registrationRepository.save(reg));
        }

        if (createdRegistrations.isEmpty()) {
            throw new RuntimeException("Please select valid slots to register for course " + course.getCode());
        }

        return getStudentRegistrations(student.getUsername());
    }

    @Transactional
    public List<RegistrationResponseDTO> modifySlot(ModifySlotRequest request) {
        User student = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Student not found: " + request.getUsername()));

        validateRegistrationWindow(student);

        // Find candidate new slot
        CourseSlot newSlot = null;
        if (request.getNewSlotId() != null) {
            newSlot = courseSlotRepository.findById(request.getNewSlotId()).orElse(null);
        }
        if (newSlot == null && request.getNewSlotCode() != null && request.getCourseCode() != null) {
            List<CourseSlot> slots = courseSlotRepository.findByCourseCode(request.getCourseCode().trim().toUpperCase());
            newSlot = slots.stream()
                    .filter(s -> s.getSlotCode().equalsIgnoreCase(request.getNewSlotCode().trim()))
                    .findFirst()
                    .orElse(null);
        }

        if (newSlot == null) {
            throw new RuntimeException("Selected alternate slot not found.");
        }

        // Find existing registration to replace
        List<Registration> regs = registrationRepository.findByStudentUsernameAndStatus(student.getUsername(), "REGISTERED");
        final CourseSlot resolvedNewSlot = newSlot;

        Registration targetReg = null;
        if (request.getOldSlotId() != null) {
            targetReg = regs.stream()
                    .filter(r -> r.getCourseSlot() != null && r.getCourseSlot().getId().equals(request.getOldSlotId()))
                    .findFirst()
                    .orElse(null);
        }

        if (targetReg == null && request.getCourseCode() != null) {
            // Match same course AND same slot type (THEORY -> THEORY, LAB -> LAB)
            targetReg = regs.stream()
                    .filter(r -> r.getCourse() != null && r.getCourse().getCode().equalsIgnoreCase(request.getCourseCode().trim())
                            && r.getCourseSlot() != null && r.getCourseSlot().getSlotType().equalsIgnoreCase(resolvedNewSlot.getSlotType()))
                    .findFirst()
                    .orElse(null);

            // Fallback to match by course code if no slot type match
            if (targetReg == null) {
                targetReg = regs.stream()
                        .filter(r -> r.getCourse() != null && r.getCourse().getCode().equalsIgnoreCase(request.getCourseCode().trim()))
                        .findFirst()
                        .orElse(null);
            }
        }

        if (targetReg == null) {
            throw new RuntimeException("No existing registration found to modify for course " + request.getCourseCode());
        }

        CourseSlot oldSlot = targetReg.getCourseSlot();

        if (oldSlot.getId().equals(newSlot.getId())) {
            throw new RuntimeException("You are already registered in slot " + newSlot.getSlotCode());
        }

        int newAvail = newSlot.getAvailableSeats() != null ? newSlot.getAvailableSeats() : (newSlot.getTotalSeats() != null ? newSlot.getTotalSeats() : 70);
        if (newAvail <= 0) {
            throw new RuntimeException("No available seats in slot " + newSlot.getSlotCode());
        }

        // Release old seat
        int oldAvail = oldSlot.getAvailableSeats() != null ? oldSlot.getAvailableSeats() : (oldSlot.getTotalSeats() != null ? oldSlot.getTotalSeats() : 70);
        oldSlot.setAvailableSeats(oldAvail + 1);
        courseSlotRepository.save(oldSlot);

        // Claim new seat
        newSlot.setAvailableSeats(newAvail - 1);
        courseSlotRepository.save(newSlot);

        // Update registration record
        targetReg.setCourseSlot(newSlot);
        targetReg.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(targetReg);

        return getStudentRegistrations(student.getUsername());
    }

    @Transactional
    public void deleteRegistration(Long registrationId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration record not found: " + registrationId));

        validateRegistrationWindow(reg.getStudent());

        CourseSlot slot = reg.getCourseSlot();
        slot.setAvailableSeats(slot.getAvailableSeats() + 1);
        courseSlotRepository.save(slot);

        registrationRepository.delete(reg);
    }

    @Transactional
    public void dropCourseForStudent(String username, String courseCode) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found: " + username));

        validateRegistrationWindow(student);

        List<Registration> regs = registrationRepository.findByStudentUsernameAndStatus(username, "REGISTERED");

        List<Registration> toDelete = regs.stream()
                .filter(r -> r.getCourse().getCode().equalsIgnoreCase(courseCode.trim()))
                .collect(Collectors.toList());

        for (Registration reg : toDelete) {
            CourseSlot slot = reg.getCourseSlot();
            slot.setAvailableSeats(slot.getAvailableSeats() + 1);
            courseSlotRepository.save(slot);
            registrationRepository.delete(reg);
        }
    }

    @Transactional
    public void batchDropCourses(String username, List<String> courseCodes) {
        if (courseCodes != null) {
            User student = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + username));
            validateRegistrationWindow(student);

            for (String code : courseCodes) {
                dropCourseForStudent(username, code);
            }
        }
    }
}
