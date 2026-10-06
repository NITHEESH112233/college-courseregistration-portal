package course.registration.demo.repository;

import course.registration.demo.model.CourseSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseSlotRepository extends JpaRepository<CourseSlot, Long> {
    List<CourseSlot> findByCourseId(Long courseId);
    List<CourseSlot> findByCourseCode(String courseCode);
    List<CourseSlot> findByCourseCodeAndSlotType(String courseCode, String slotType);
}
