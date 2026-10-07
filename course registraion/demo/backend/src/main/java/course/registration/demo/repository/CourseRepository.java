package course.registration.demo.repository;

import course.registration.demo.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByActiveTrueOrderByCourseCodeAsc();

    List<Course> findByBasketCategoryAndActiveTrueOrderByCourseCodeAsc(
            Course.BasketCategory basketCategory
    );

    @Query("""
        SELECT c
        FROM Course c
        WHERE c.active = true
        AND (
            LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(c.courseType) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        ORDER BY c.courseCode ASC
    """)
    List<Course> searchActiveCourses(@Param("q") String q);
}