package course.registration.demo.repository;

import course.registration.demo.model.RegistrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationConfigRepository extends JpaRepository<RegistrationConfig, Long> {
}
