package course.registration.demo.controller;

import course.registration.demo.dto.ApiResponse;
import course.registration.demo.dto.CourseDTO;
import course.registration.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getAllCourses(
            @RequestParam(required = false) String basket,
            @RequestParam(required = false) String username) {
        try {
            List<CourseDTO> list = courseService.getAllCourses(basket, username);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseByCode(
            @PathVariable String code,
            @RequestParam(required = false) String username) {
        try {
            CourseDTO course = courseService.getCourseByCode(code, username);
            return ResponseEntity.ok(ApiResponse.ok(course));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
