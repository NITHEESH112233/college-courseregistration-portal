package course.registration.demo.service;

import course.registration.demo.model.*;
import course.registration.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseSlotRepository courseSlotRepository;
    private final RegistrationRepository registrationRepository;
    private final MasterlistRepository masterlistRepository;
    private final NotificationRepository notificationRepository;
    private final RegistrationConfigRepository configRepository;

    @Override
    public void run(String... args) {
        log.info("Checking database initialization...");

        // 1. Initialize Single Admin Account
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            adminUser = User.builder()
                    .username("admin")
                    .password("admin123")
                    .fullName("System Administrator")
                    .email("admin@vitap.ac.in")
                    .role("ADMIN")
                    .program("Administrative Staff")
                    .semester("N/A")
                    .cgpa(10.0)
                    .earnedCredits(0)
                    .maxCredits(0)
                    .minCredits(0)
                    .slotActive(true)
                    .build();
            userRepository.save(adminUser);
        } else {
            adminUser.setPassword("admin123");
            userRepository.save(adminUser);
        }

        // 2. Initialize Registration Config
        if (configRepository.count() == 0) {
            RegistrationConfig cfg = RegistrationConfig.builder()
                    .semesterName("Winter Semester 2024")
                    .isRegistrationOpen(true)
                    .remainingHours(2)
                    .remainingMinutes(45)
                    .remainingSeconds(30)
                    .minCredits(16)
                    .maxCredits(27)
                    .build();
            configRepository.save(cfg);
        }

        // 3. Initialize Courses & Slots
        if (courseRepository.count() == 0) {
            initCourses();
        }

        // 4. Initialize System Notifications
        if (notificationRepository.count() == 0) {
            initNotifications(adminUser);
        }

        log.info("Database initialization completed successfully.");
    }

    private void initCourses() {
        // Course 1: SWE3004 (PE)
        Course swe3004 = Course.builder()
                .code("SWE3004")
                .title("Front End Design and Testing")
                .basket("PE")
                .credits(4.0)
                .ltpjc("3 0 2 0 4.0")
                .componentType("Embedded Theory / Embedded Lab")
                .description("Fundamental concepts of frontend UI/UX architecture, responsive CSS layout systems, React.js, and client-side testing frameworks.")
                .build();
        swe3004 = courseRepository.save(swe3004);

        courseSlotRepository.save(CourseSlot.builder().course(swe3004).slotType("THEORY").slotCode("A1+TA1").venue("CB-204").facultyName("Dr. Ananya Sharma").totalSeats(70).availableSeats(65).dayTiming("Tue 12:00 / Wed 12:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(swe3004).slotType("THEORY").slotCode("E2+TE2").venue("CB-524").facultyName("Prof. SCOPE Dig Crs Faculty-4").totalSeats(70).availableSeats(69).dayTiming("Mon 14:00 / Thu 14:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(swe3004).slotType("THEORY").slotCode("E1+TE1").venue("CB-225").facultyName("Prof. SCOPE Dig Crs Faculty-4").totalSeats(70).availableSeats(68).dayTiming("Tue 16:00 / Fri 16:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(swe3004).slotType("LAB").slotCode("L11+L12").venue("CB-121").facultyName("Prof. SCOPE Dig Crs Faculty-20").totalSeats(70).availableSeats(65).dayTiming("Thu 14:00 - 15:50").build());
        courseSlotRepository.save(CourseSlot.builder().course(swe3004).slotType("LAB").slotCode("L27+L28").venue("CB-303").facultyName("Prof. SCOPE Dig Crs Faculty-20").totalSeats(70).availableSeats(65).dayTiming("Fri 14:00 - 15:50").build());
        courseSlotRepository.save(CourseSlot.builder().course(swe3004).slotType("LAB").slotCode("L13+L14").venue("CB-405").facultyName("Kandru Lakshmi Sai Praneeth").totalSeats(60).availableSeats(58).dayTiming("Mon 08:00 - 09:50").build());

        // Course 2: CSE1001 (PC)
        Course cse1001 = Course.builder()
                .code("CSE1001")
                .title("Data Structures and Algorithms")
                .basket("PC")
                .credits(4.0)
                .ltpjc("3 0 2 0 4.0")
                .componentType("Embedded Theory / Embedded Lab")
                .description("Fundamental concepts of data structures and the algorithms that operate on them. Includes arrays, linked lists, trees, and graphs.")
                .build();
        cse1001 = courseRepository.save(cse1001);

        courseSlotRepository.save(CourseSlot.builder().course(cse1001).slotType("THEORY").slotCode("A2+TA2").venue("CB-112").facultyName("Dr. Rajesh K.").totalSeats(60).availableSeats(45).dayTiming("Mon 08:00 / Wed 08:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse1001).slotType("THEORY").slotCode("B2+TB2").venue("AB-204").facultyName("Prof. Anita Sen").totalSeats(60).availableSeats(32).dayTiming("Tue 09:00 / Thu 09:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse1001).slotType("LAB").slotCode("L1+L2").venue("LAB-1").facultyName("Dr. Rajesh K.").totalSeats(40).availableSeats(28).dayTiming("Tue 14:00 - 15:50").build());

        // Course 3: CSE2001 (PC)
        Course cse2001 = Course.builder()
                .code("CSE2001")
                .title("Computer Architecture and Organization")
                .basket("PC")
                .credits(4.0)
                .ltpjc("3 0 2 0 4.0")
                .componentType("Embedded Theory / Embedded Lab")
                .description("Design and analysis of computer systems, memory hierarchy, pipelining, and instruction set architectures.")
                .build();
        cse2001 = courseRepository.save(cse2001);

        courseSlotRepository.save(CourseSlot.builder().course(cse2001).slotType("THEORY").slotCode("B2+TB2").venue("AB1-311").facultyName("Prof. Rajiv Menon").totalSeats(60).availableSeats(42).dayTiming("Tue 10:00 / Thu 10:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse2001).slotType("THEORY").slotCode("C2+TC2").venue("CB-208").facultyName("Dr. K. Srinivas").totalSeats(60).availableSeats(26).dayTiming("Wed 11:00 / Fri 11:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse2001).slotType("LAB").slotCode("L11+L12").venue("LAB-4").facultyName("Prof. Rajiv Menon").totalSeats(40).availableSeats(30).dayTiming("Wed 14:00 - 15:50").build());

        // Course 4: CSE3002 (PC)
        Course cse3002 = Course.builder()
                .code("CSE3002")
                .title("Machine Learning")
                .basket("PC")
                .credits(4.0)
                .ltpjc("3 0 2 0 4.0")
                .componentType("Embedded Theory / Embedded Lab")
                .description("Supervised learning, unsupervised learning, deep neural networks, model evaluation, and loss functions.")
                .build();
        cse3002 = courseRepository.save(cse3002);

        courseSlotRepository.save(CourseSlot.builder().course(cse3002).slotType("THEORY").slotCode("A1+TA1").venue("CB-204").facultyName("Dr. K. Raman").totalSeats(60).availableSeats(48).dayTiming("Tue 08:00 / Wed 08:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse3002).slotType("THEORY").slotCode("B1+TB1").venue("CB-302").facultyName("Dr. M. Roy").totalSeats(60).availableSeats(22).dayTiming("Tue 09:00 / Wed 09:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse3002).slotType("THEORY").slotCode("C1+TC1").venue("AB-105").facultyName("Prof. S. Rao").totalSeats(60).availableSeats(15).dayTiming("Tue 10:00 / Wed 10:00").build());

        // Course 5: CSE2005 (PC)
        Course cse2005 = Course.builder()
                .code("CSE2005")
                .title("Database Management Systems")
                .basket("PC")
                .credits(4.0)
                .ltpjc("3 0 2 0 4.0")
                .componentType("Embedded Theory / Embedded Lab")
                .description("Relational models, SQL, normalization, concurrency control, query optimization, indexing and transactions.")
                .build();
        cse2005 = courseRepository.save(cse2005);

        courseSlotRepository.save(CourseSlot.builder().course(cse2005).slotType("THEORY").slotCode("D1+TD1").venue("CB-101").facultyName("Dr. Sharma R.").totalSeats(60).availableSeats(54).dayTiming("Tue 14:00 / Wed 14:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse2005).slotType("THEORY").slotCode("E1+TE1").venue("CB-203").facultyName("Prof. K. Verma").totalSeats(60).availableSeats(30).dayTiming("Tue 16:00 / Wed 16:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse2005).slotType("LAB").slotCode("L31+L32").venue("TT-401").facultyName("Dr. Sharma R.").totalSeats(40).availableSeats(35).dayTiming("Tue 14:00 - 15:50").build());

        // Course 6: CSE3003 (PC)
        Course cse3003 = Course.builder()
                .code("CSE3003")
                .title("Computer Networks")
                .basket("PC")
                .credits(3.0)
                .ltpjc("3 0 0 0 3.0")
                .componentType("Theory Only")
                .description("OSI model, TCP/IP protocol suite, routing algorithms, transport layer protocols and network security.")
                .build();
        cse3003 = courseRepository.save(cse3003);

        courseSlotRepository.save(CourseSlot.builder().course(cse3003).slotType("THEORY").slotCode("E1+TE1").venue("CB-401").facultyName("Prof. Iyer").totalSeats(60).availableSeats(30).dayTiming("Tue 16:00 / Wed 16:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse3003).slotType("THEORY").slotCode("F1+TF1").venue("AB-304").facultyName("Dr. Singh").totalSeats(60).availableSeats(12).dayTiming("Wed 17:00 / Fri 17:00").build());

        // Course 7: CSE2505 (PC)
        Course cse2505 = Course.builder()
                .code("CSE2505")
                .title("Operating Systems")
                .basket("PC")
                .credits(4.0)
                .ltpjc("3 0 2 0 4.0")
                .componentType("Embedded Theory / Embedded Lab")
                .description("Process synchronization, CPU scheduling algorithms, memory management, virtual memory and file systems.")
                .build();
        cse2505 = courseRepository.save(cse2505);

        courseSlotRepository.save(CourseSlot.builder().course(cse2505).slotType("THEORY").slotCode("B2+TB2").venue("CB-305").facultyName("Prof. Das").totalSeats(60).availableSeats(40).dayTiming("Thu 09:00 / Fri 09:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse2505).slotType("THEORY").slotCode("C2+TC2").venue("AB-108").facultyName("Dr. Ali").totalSeats(60).availableSeats(19).dayTiming("Wed 10:00 / Fri 10:00").build());

        // Course 8: CSE2004 (PE)
        Course cse2004 = Course.builder()
                .code("CSE2004")
                .title("Software Engineering")
                .basket("PE")
                .credits(3.0)
                .ltpjc("3 0 0 0 3.0")
                .componentType("Theory Only")
                .description("Agile methodologies, software requirements specification, architectural patterns and software quality assurance.")
                .build();
        cse2004 = courseRepository.save(cse2004);

        courseSlotRepository.save(CourseSlot.builder().course(cse2004).slotType("THEORY").slotCode("G1+TG1").venue("CB-502").facultyName("Dr. Gupta").totalSeats(60).availableSeats(55).dayTiming("Wed 18:00 / Thu 16:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(cse2004).slotType("THEORY").slotCode("H1+TH1").venue("AB-402").facultyName("Prof. Nanda").totalSeats(60).availableSeats(25).dayTiming("Fri 18:00 / Sat 18:00").build());

        // Course 9: MAT2002 (UC)
        Course mat2002 = Course.builder()
                .code("MAT2002")
                .title("Applied Statistics and Probability")
                .basket("UC")
                .credits(3.0)
                .ltpjc("3 0 0 0 3.0")
                .componentType("Theory Only")
                .description("Probability distributions, sampling theory, hypothesis testing, ANOVA, and regression analysis.")
                .build();
        mat2002 = courseRepository.save(mat2002);

        courseSlotRepository.save(CourseSlot.builder().course(mat2002).slotType("THEORY").slotCode("B1+TB1").venue("AB-101").facultyName("Dr. S. Mukherjee").totalSeats(60).availableSeats(60).dayTiming("Tue 09:00 / Wed 09:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(mat2002).slotType("THEORY").slotCode("F1+TF1").venue("CB-104").facultyName("Dr. S. Mukherjee").totalSeats(60).availableSeats(60).dayTiming("Tue 18:00 / Fri 18:00").build());

        // Course 10: MAT1011 (UC)
        Course mat1011 = Course.builder()
                .code("MAT1011")
                .title("Calculus for Engineers")
                .basket("UC")
                .credits(4.0)
                .ltpjc("3 1 0 0 4.0")
                .componentType("Theory Only")
                .description("Differential and integral calculus of one and several variables, applications to engineering problems.")
                .build();
        mat1011 = courseRepository.save(mat1011);

        courseSlotRepository.save(CourseSlot.builder().course(mat1011).slotType("THEORY").slotCode("C1+TC1").venue("CB-102").facultyName("Dr. K. Srinivasan").totalSeats(60).availableSeats(48).dayTiming("Wed 10:00 / Thu 10:00").build());

        // Course 11: PHY1701 (UC) / PHY1001
        Course phy1001 = Course.builder()
                .code("PHY1001")
                .title("Engineering Physics")
                .basket("UC")
                .credits(4.0)
                .ltpjc("3 0 2 0 4.0")
                .componentType("Embedded Theory / Embedded Lab")
                .description("Introduction to quantum mechanics, solid state physics, optics, laser principles and semiconductor devices.")
                .build();
        phy1001 = courseRepository.save(phy1001);

        courseSlotRepository.save(CourseSlot.builder().course(phy1001).slotType("THEORY").slotCode("D2+TD2").venue("CB-401").facultyName("Dr. Meera Patel").totalSeats(60).availableSeats(45).dayTiming("Thu 11:00 / Fri 11:00").build());
        courseSlotRepository.save(CourseSlot.builder().course(phy1001).slotType("LAB").slotCode("L19+L20").venue("PHY-LAB").facultyName("Dr. Meera Patel").totalSeats(40).availableSeats(30).dayTiming("Fri 08:00 - 09:50").build());

        // Course 12: MGT1022 (UE)
        Course mgt1022 = Course.builder()
                .code("MGT1022")
                .title("Lean Start-up Management")
                .basket("UE")
                .credits(3.0)
                .ltpjc("3 0 0 0 3.0")
                .componentType("Theory Only")
                .description("Principles of innovation, business model canvas, customer discovery, minimum viable product, and venture growth.")
                .build();
        mgt1022 = courseRepository.save(mgt1022);

        courseSlotRepository.save(CourseSlot.builder().course(mgt1022).slotType("THEORY").slotCode("D2+TD2").venue("AB-308").facultyName("Prof. Verma").totalSeats(60).availableSeats(50).dayTiming("Thu 10:00 / Fri 16:00").build());

        // Course 13: HUM1021 (UC)
        Course hum1021 = Course.builder()
                .code("HUM1021")
                .title("Ethics and Values")
                .basket("UC")
                .credits(2.0)
                .ltpjc("2 0 0 0 2.0")
                .componentType("Theory Only")
                .description("Human values, professional ethics, constitutional values, environmental ethics and integrity.")
                .build();
        hum1021 = courseRepository.save(hum1021);

        courseSlotRepository.save(CourseSlot.builder().course(hum1021).slotType("THEORY").slotCode("A1").venue("CB-501").facultyName("Dr. Charles David").totalSeats(75).availableSeats(75).dayTiming("Tue 18:00").build());
    }

    private void initNotifications(User adminUser) {
        notificationRepository.save(Notification.builder()
                .student(null)
                .title("Registration Slot Modification Closing Soon")
                .category("registration")
                .type("urgent")
                .unread(true)
                .timeAgo("10 minutes ago")
                .body("The Course Slot Modification window for Fall Semester 2024-25 will officially conclude today at 5:00 PM IST. Please verify your registered slots before the server lock.")
                .actionText("Modify Registered Slots")
                .actionLink("modify_slots.html")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build());

        notificationRepository.save(Notification.builder()
                .student(null)
                .title("Additional Capacity Added: Machine Learning (CSE3002)")
                .category("registration")
                .type("registration")
                .unread(true)
                .timeAgo("45 minutes ago")
                .body("30 extra seats have been added to slot A1+TA1 under Dr. K. Raman in Central Block Room 204. Seats are available on first-come-first-serve basis.")
                .actionText("View in Slot Selection")
                .actionLink("slot_selection.html")
                .createdAt(LocalDateTime.now().minusMinutes(45))
                .build());

        notificationRepository.save(Notification.builder()
                .student(null)
                .title("FCFS Masterlist Verified & Ready")
                .category("registration")
                .type("success")
                .unread(true)
                .timeAgo("2 hours ago")
                .body("Your 5-course pre-registration Masterlist has been validated with 0 time clashes. Instant 1-Click execution is armed and ready.")
                .actionText("Open Saved Masterlist")
                .actionLink("masterlist.html")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build());

        notificationRepository.save(Notification.builder()
                .student(null)
                .title("Faculty Assignment Update for Database Systems")
                .category("academic")
                .type("academic")
                .unread(false)
                .timeAgo("Yesterday, 3:30 PM")
                .body("Dr. Sharma R. has been allocated as primary instructor for CSE2005 theory slot D1+TD1. Classroom venue remains CB-101.")
                .actionText("View Academic Profile")
                .actionLink("academic_profile.html")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build());

        notificationRepository.save(Notification.builder()
                .student(null)
                .title("Draft Timetable Schedule Released")
                .category("academic")
                .type("academic")
                .unread(false)
                .timeAgo("2 days ago")
                .body("Your provisional weekly timetable schedule has been populated based on current slot reservations. Check for any afternoon lab overlaps.")
                .actionText("Check Timetable")
                .actionLink("timetable.html")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build());

        notificationRepository.save(Notification.builder()
                .student(null)
                .title("Portal Security: New Login Session")
                .category("system")
                .type("security")
                .unread(false)
                .timeAgo("3 days ago")
                .body("A new login was recorded for registration number 21BCE0000 from Google Chrome on Windows. If this was not you, update your credentials.")
                .actionText("Security Settings")
                .actionLink("settings.html")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build());
    }
}
