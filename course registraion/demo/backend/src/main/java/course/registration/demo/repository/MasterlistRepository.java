package course.registration.demo.repository;

import course.registration.demo.model.MasterlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MasterlistRepository extends JpaRepository<MasterlistEntry, Long> {
    List<MasterlistEntry> findByStudentUsernameOrderByRankNumberAsc(String username);
    List<MasterlistEntry> findByCourseId(Long courseId);
    void deleteByStudentUsername(String username);
    void deleteByCourseId(Long courseId);
}
