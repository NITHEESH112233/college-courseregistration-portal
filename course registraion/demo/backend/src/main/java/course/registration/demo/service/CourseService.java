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

    public List<CourseDTO> getAllCourses(String basket, String username) {
        List<Course> courses;
        if (basket != null && !basket.isBlank()) {
            courses = courseRepository.findByBasketIgnoreCase(basket.trim());
        } else {
            courses = courseRepository.findAll();
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
                .basket(course.getBasket())
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
