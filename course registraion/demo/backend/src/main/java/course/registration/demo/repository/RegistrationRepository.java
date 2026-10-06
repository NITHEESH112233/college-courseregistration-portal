package course.registration.demo.repository;

import course.registration.demo.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudentId(Long studentId);
    List<Registration> findByCourseId(Long courseId);
    List<Registration> findByStudentUsername(String username);
    List<Registration> findByStudentUsernameAndStatus(String username, String status);
    Optional<Registration> findByStudentUsernameAndCourseCode(String username, String courseCode);
    Optional<Registration> findByStudentUsernameAndCourseSlotId(String username, Long slotId);
    boolean existsByStudentUsernameAndCourseCodeAndStatus(String username, String courseCode, String status);
}
