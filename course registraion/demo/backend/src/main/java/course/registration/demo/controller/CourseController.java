package course.registration.demo.controller;

import course.registration.demo.entity.Course;
import course.registration.demo.repository.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * GET /api/courses
     *
     * Returns all active courses.
     */
    @GetMapping
    public ResponseEntity<List<Course>> getAllActiveCourses() {

        List<Course> courses =
                courseRepository.findByActiveTrueOrderByCourseCodeAsc();

        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/courses/basket/{category}
     *
     * Supported categories:
     * UC
     * UE
     * PC
     * PE
     */
    @GetMapping("/basket/{category}")
    public ResponseEntity<?> getCoursesByBasket(
            @PathVariable String category
    ) {

        try {

            Course.BasketCategory basketCategory =
                    Course.BasketCategory.valueOf(
                            category.toUpperCase(Locale.ROOT)
                    );

            List<Course> courses =
                    courseRepository
                            .findByBasketCategoryAndActiveTrueOrderByCourseCodeAsc(
                                    basketCategory
                            );

            return ResponseEntity.ok(courses);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid basket category. Use UC, UE, PC or PE.");
        }
    }

    /**
     * GET /api/courses/search?q=...
     *
     * Live search by:
     * - course code
     * - title
     * - course type
     */
    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(
            @RequestParam(defaultValue = "") String q
    ) {

        if (q == null || q.trim().isEmpty()) {

            return ResponseEntity.ok(
                    courseRepository.findByActiveTrueOrderByCourseCodeAsc()
            );
        }

        List<Course> courses =
                courseRepository.searchActiveCourses(q.trim());

        return ResponseEntity.ok(courses);
    }
}