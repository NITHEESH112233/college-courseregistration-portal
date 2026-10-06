package course.registration.demo.service;

import course.registration.demo.dto.ModifySlotRequest;
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

    public List<RegistrationResponseDTO> getStudentRegistrations(String username) {
        List<Registration> list = registrationRepository.findByStudentUsernameAndStatus(username, "REGISTERED");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        return list.stream().map(r -> RegistrationResponseDTO.builder()
                .id(r.getId())
                .courseId(r.getCourse().getId())
                .courseCode(r.getCourse().getCode())
                .courseTitle(r.getCourse().getTitle())
                .basket(r.getCourse().getBasket())
                .componentType(r.getCourseSlot().getSlotType().equalsIgnoreCase("LAB") ? "Lab" : "Theory")
                .slotCode(r.getCourseSlot().getSlotCode())
                .facultyName(r.getCourseSlot().getFacultyName())
                .venue(r.getCourseSlot().getVenue())
                .credits(r.getCourseSlot().getSlotType().equalsIgnoreCase("LAB") ? 1.0 : (r.getCourse().getCredits() > 1 ? r.getCourse().getCredits() - (r.getCourse().getComponentType().contains("Lab") ? 1.0 : 0.0) : r.getCourse().getCredits()))
                .registeredAt(r.getRegisteredAt() != null ? r.getRegisteredAt().format(dtf) : "Recent")
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public List<RegistrationResponseDTO> registerCourse(RegistrationRequest request) {
        User student = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Student not found: " + request.getUsername()));

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
            theorySlot = slots.stream()
                    .filter(s -> s.getSlotCode().equalsIgnoreCase(request.getTheorySlotCode().trim()))
                    .findFirst()
                    .orElse(null);
        }

        if (theorySlot != null) {
            if (theorySlot.getAvailableSeats() <= 0) {
                throw new RuntimeException("No seats available in theory slot: " + theorySlot.getSlotCode());
            }
            theorySlot.setAvailableSeats(theorySlot.getAvailableSeats() - 1);
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
            labSlot = slots.stream()
                    .filter(s -> s.getSlotCode().equalsIgnoreCase(request.getLabSlotCode().trim()))
                    .findFirst()
                    .orElse(null);
        }

        if (labSlot != null) {
            if (labSlot.getAvailableSeats() <= 0) {
                throw new RuntimeException("No seats available in lab slot: " + labSlot.getSlotCode());
            }
            labSlot.setAvailableSeats(labSlot.getAvailableSeats() - 1);
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

        // Find existing registration
        List<Registration> regs = registrationRepository.findByStudentUsernameAndStatus(student.getUsername(), "REGISTERED");
        Registration targetReg = regs.stream()
                .filter(r -> r.getCourse().getCode().equalsIgnoreCase(request.getCourseCode()) ||
                             (request.getOldSlotId() != null && r.getCourseSlot().getId().equals(request.getOldSlotId())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existing registration found to modify for course " + request.getCourseCode()));

        CourseSlot oldSlot = targetReg.getCourseSlot();

        CourseSlot newSlot = null;
        if (request.getNewSlotId() != null) {
            newSlot = courseSlotRepository.findById(request.getNewSlotId()).orElse(null);
        }
        if (newSlot == null && request.getNewSlotCode() != null) {
            List<CourseSlot> slots = courseSlotRepository.findByCourseCode(targetReg.getCourse().getCode());
            newSlot = slots.stream()
                    .filter(s -> s.getSlotCode().equalsIgnoreCase(request.getNewSlotCode().trim()))
                    .findFirst()
                    .orElse(null);
        }

        if (newSlot == null) {
            throw new RuntimeException("Selected alternate slot not found.");
        }

        if (newSlot.getId().equals(oldSlot.getId())) {
            throw new RuntimeException("You are already registered in slot " + newSlot.getSlotCode());
        }

        if (newSlot.getAvailableSeats() <= 0) {
            throw new RuntimeException("No available seats in slot " + newSlot.getSlotCode());
        }

        // Release old seat
        oldSlot.setAvailableSeats(oldSlot.getAvailableSeats() + 1);
        courseSlotRepository.save(oldSlot);

        // Claim new seat
        newSlot.setAvailableSeats(newSlot.getAvailableSeats() - 1);
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

        CourseSlot slot = reg.getCourseSlot();
        slot.setAvailableSeats(slot.getAvailableSeats() + 1);
        courseSlotRepository.save(slot);

        registrationRepository.delete(reg);
    }

    @Transactional
    public void dropCourseForStudent(String username, String courseCode) {
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
            for (String code : courseCodes) {
                dropCourseForStudent(username, code);
            }
        }
    }
}
