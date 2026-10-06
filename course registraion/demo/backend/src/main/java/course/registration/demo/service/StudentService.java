package course.registration.demo.service;

import course.registration.demo.dto.StudentProfileDTO;
import course.registration.demo.model.Registration;
import course.registration.demo.model.User;
import course.registration.demo.repository.RegistrationRepository;
import course.registration.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

    public StudentProfileDTO getStudentProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found: " + username));

        List<Registration> regs = registrationRepository.findByStudentUsernameAndStatus(username, "REGISTERED");

        int registeredCredits = 0;
        Map<String, Integer> basketCredits = new HashMap<>();
        basketCredits.put("UC", 35);
        basketCredits.put("UE", 12);
        basketCredits.put("PC", 50);
        basketCredits.put("PE", 23);

        Set<String> countedCourses = new HashSet<>();
        for (Registration r : regs) {
            String courseCode = r.getCourse().getCode();
            if (!countedCourses.contains(courseCode)) {
                countedCourses.add(courseCode);
                int cr = r.getCourse().getCredits() != null ? r.getCourse().getCredits().intValue() : 0;
                registeredCredits += cr;
                String b = r.getCourse().getBasket() != null ? r.getCourse().getBasket().toUpperCase() : "PC";
                basketCredits.put(b, basketCredits.getOrDefault(b, 0) + cr);
            }
        }

        int earnedTotal = (user.getEarnedCredits() != null ? user.getEarnedCredits() : 120);

        return StudentProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .program(user.getProgram() != null ? user.getProgram() : "B.Tech Computer Science & Engineering")
                .semester(user.getSemester() != null ? user.getSemester() : "Semester 6")
                .cgpa(user.getCgpa() != null ? user.getCgpa() : 8.95)
                .earnedCredits(earnedTotal)
                .registeredCredits(registeredCredits)
                .minCredits(user.getMinCredits() != null ? user.getMinCredits() : 16)
                .maxCredits(user.getMaxCredits() != null ? user.getMaxCredits() : 27)
                .slotActive(user.getSlotActive() != null ? user.getSlotActive() : true)
                .basketCredits(basketCredits)
                .build();
    }

    public StudentProfileDTO updatePreferences(String username, String fullName, String email, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found: " + username));

        if (fullName != null && !fullName.isBlank()) user.setFullName(fullName.trim());
        if (email != null && !email.isBlank()) user.setEmail(email.trim());
        if (newPassword != null && !newPassword.isBlank()) user.setPassword(newPassword.trim());

        userRepository.save(user);
        return getStudentProfile(username);
    }
}
