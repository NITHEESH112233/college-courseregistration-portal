package course.registration.demo.service;

import course.registration.demo.dto.CourseDTO;
import course.registration.demo.dto.CourseSlotDTO;
import course.registration.demo.model.Course;
import course.registration.demo.model.CourseSlot;
import course.registration.demo.repository.CourseRepository;
import course.registration.demo.repository.CourseSlotRepository;
import course.registration.demo.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSlotRepository courseSlotRepository;
    private final RegistrationRepository registrationRepository;

    public static String normalizeBasket(String raw) {
        if (raw == null || raw.isBlank()) return "PC";
        String upper = raw.trim().toUpperCase();
        if (upper.contains("UE") || upper.contains("UNIVERSITY ELECTIVE") || upper.contains("OPEN ELECTIVE")) {
            return "UE";
        }
        if (upper.contains("UC") || upper.contains("UNIVERSITY CORE")) {
            return "UC";
        }
        if (upper.contains("PE") || upper.contains("PROGRAM ELECTIVE") || upper.contains("SPECIALIZED ELECTIVE")) {
            return "PE";
        }
        if (upper.contains("PC") || upper.contains("PROGRAM CORE") || upper.contains("MAJOR CORE")) {
            return "PC";
        }
        return upper;
    }

    public List<CourseDTO> getAllCourses(String basket, String username) {
        List<Course> courses = courseRepository.findAll();

        // Self-heal and normalize any unnormalized course basket strings in database
        for (Course c : courses) {
            String norm = normalizeBasket(c.getBasket());
            if (!norm.equals(c.getBasket())) {
                c.setBasket(norm);
                try {
                    courseRepository.save(c);
                } catch (Exception ignored) {}
            }
        }

        if (basket != null && !basket.isBlank()) {
            String target = normalizeBasket(basket);
            courses = courses.stream()
                    .filter(c -> target.equalsIgnoreCase(normalizeBasket(c.getBasket())))
                    .collect(Collectors.toList());
        }

        return courses.stream()
                .map(c -> toCourseDTO(c, username))
                .collect(Collectors.toList());
    }

    public CourseDTO getCourseByCode(String code, String username) {
        Course course = courseRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Course not found: " + code));
        return toCourseDTO(course, username);
    }

    private CourseDTO toCourseDTO(Course course, String username) {
        boolean isRegistered = false;
        if (username != null && !username.isBlank()) {
            isRegistered = registrationRepository.existsByStudentUsernameAndCourseCodeAndStatus(username, course.getCode(), "REGISTERED");
        }

        List<CourseSlot> allSlots = courseSlotRepository.findByCourseId(course.getId());

        List<CourseSlotDTO> theorySlots = allSlots.stream()
                .filter(s -> "THEORY".equalsIgnoreCase(s.getSlotType()))
                .map(this::toSlotDTO)
                .collect(Collectors.toList());

        List<CourseSlotDTO> labSlots = allSlots.stream()
                .filter(s -> "LAB".equalsIgnoreCase(s.getSlotType()))
                .map(this::toSlotDTO)
                .collect(Collectors.toList());

        return CourseDTO.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .basket(normalizeBasket(course.getBasket()))
                .credits(course.getCredits())
                .ltpjc(course.getLtpjc())
                .componentType(course.getComponentType())
                .description(course.getDescription())
                .isRegistered(isRegistered)
                .theorySlots(theorySlots)
                .labSlots(labSlots)
                .build();
    }

    public CourseSlotDTO toSlotDTO(CourseSlot slot) {
        return CourseSlotDTO.builder()
                .id(slot.getId())
                .courseId(slot.getCourse() != null ? slot.getCourse().getId() : null)
                .courseCode(slot.getCourse() != null ? slot.getCourse().getCode() : null)
                .courseName(slot.getCourse() != null ? slot.getCourse().getTitle() : null)
                .slotType(slot.getSlotType())
                .slotCode(slot.getSlotCode())
                .venue(slot.getVenue())
                .facultyName(slot.getFacultyName())
                .totalSeats(slot.getTotalSeats())
                .availableSeats(slot.getAvailableSeats())
                .dayTiming(slot.getDayTiming())
                .build();
    }
}
