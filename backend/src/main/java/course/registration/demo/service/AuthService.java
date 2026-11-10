package course.registration.demo.service;

import course.registration.demo.dto.LoginRequest;
import course.registration.demo.dto.LoginResponse;
import course.registration.demo.model.User;
import course.registration.demo.repository.RegistrationRepository;
import course.registration.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

    public LoginResponse authenticate(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return LoginResponse.builder().success(false).message("Username or Registration number is required.")
                    .build();
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return LoginResponse.builder().success(false).message("Password is required.").build();
        }

        String username = request.getUsername().trim().toUpperCase();
        String password = request.getPassword().trim();

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            if (request.getRole() != null && "STUDENT".equalsIgnoreCase(request.getRole().trim())) {
                return LoginResponse.builder()
                        .success(false)
                        .message(
                                "Student record not found. Only students provisioned by the Administrator can access the portal.")
                        .build();
            } else if (request.getRole() != null && "ADMIN".equalsIgnoreCase(request.getRole().trim())) {
                return LoginResponse.builder()
                        .success(false)
                        .message("Administrator account not found: " + username)
                        .build();
            } else {
                return LoginResponse.builder()
                        .success(false)
                        .message("Account not found: " + username)
                        .build();
            }
        }

        User user = userOpt.get();

        // Check role permission only if explicitly specified in request
        if (request.getRole() != null && !request.getRole().isBlank()) {
            String requestedRole = request.getRole().trim().toUpperCase();
            if (!user.getRole().equalsIgnoreCase(requestedRole)) {
                return LoginResponse.builder()
                        .success(false)
                        .message("Access denied: Account " + username + " is registered as " + user.getRole() + ", not "
                                + requestedRole + ".")
                        .build();
            }
        }

        // Verify password
        if (!user.getPassword().equals(password)) {
            return LoginResponse.builder()
                    .success(false)
                    .message("Invalid password credentials. Please verify your password.")
                    .build();
        }

        // Check if student slot/account is active
        if (Boolean.FALSE.equals(user.getSlotActive())) {
            return LoginResponse.builder()
                    .success(false)
                    .message("Your student account is currently disabled. Please contact the Administrator.")
                    .build();
        }

        // Calculate registered credits (deduplicate course components)
        Set<String> countedCourses = new HashSet<>();
        int registeredCredits = 0;
        for (course.registration.demo.model.Registration r : registrationRepository
                .findByStudentUsernameAndStatus(username, "REGISTERED")) {
            if (r.getCourse() != null && countedCourses.add(r.getCourse().getCode())) {
                registeredCredits += (r.getCourse().getCredits() != null ? r.getCourse().getCredits().intValue() : 0);
            }
        }

        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .program(user.getProgram())
                .semester(user.getSemester())
                .cgpa(user.getCgpa())
                .earnedCredits(user.getEarnedCredits())
                .maxCredits(user.getMaxCredits())
                .registeredCredits(registeredCredits)
                .build();
    }
}
