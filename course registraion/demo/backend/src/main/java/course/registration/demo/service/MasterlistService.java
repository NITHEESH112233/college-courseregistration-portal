package course.registration.demo.service;

import course.registration.demo.dto.MasterlistDTO;
import course.registration.demo.dto.RegistrationConfigDTO;
import course.registration.demo.dto.RegistrationRequest;
import course.registration.demo.model.Course;
import course.registration.demo.model.CourseSlot;
import course.registration.demo.model.MasterlistEntry;
import course.registration.demo.model.Registration;
import course.registration.demo.model.User;
import course.registration.demo.repository.CourseRepository;
import course.registration.demo.repository.CourseSlotRepository;
import course.registration.demo.repository.MasterlistRepository;
import course.registration.demo.repository.RegistrationRepository;
import course.registration.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MasterlistService {

    private final MasterlistRepository masterlistRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseSlotRepository courseSlotRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationService registrationService;
    private final AdminService adminService;
    private final NotificationService notificationService;

    public List<MasterlistDTO> getMasterlist(String username) {
        List<MasterlistEntry> entries = masterlistRepository.findByStudentUsernameOrderByRankNumberAsc(username);

        return entries.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<MasterlistDTO> saveMasterlist(String username, List<MasterlistDTO> items) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found: " + username));

        masterlistRepository.deleteByStudentUsername(username);

        List<MasterlistEntry> newEntries = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        int rank = 1;
        for (MasterlistDTO item : items) {
            if (item.getCode() == null || item.getCode().isBlank()) continue;
            Course course = courseRepository.findByCode(item.getCode().trim().toUpperCase())
                    .orElse(null);
            if (course == null) continue;

            CourseSlot c1 = resolveSlot(course, item.getChoice1());
            CourseSlot c2 = resolveSlot(course, item.getChoice2());
            CourseSlot c3 = resolveSlot(course, item.getChoice3());

            MasterlistEntry entry = MasterlistEntry.builder()
                    .student(student)
                    .course(course)
                    .rankNumber(item.getRank() != null ? item.getRank() : rank)
                    .choice1(c1)
                    .choice2(c2)
                    .choice3(c3)
                    .status("Ready")
                    .build();

            newEntries.add(masterlistRepository.save(entry));
            rank++;
        }

        return newEntries.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private CourseSlot resolveSlot(Course course, MasterlistDTO.SlotChoiceDTO choice) {
        if (choice == null) return null;
        if (choice.getSlotId() != null) {
            return courseSlotRepository.findById(choice.getSlotId()).orElse(null);
        }
        if (choice.getSlot() != null && !choice.getSlot().isBlank()) {
            String target = choice.getSlot().replaceAll("\\s+", "").toUpperCase();
            List<CourseSlot> slots = courseSlotRepository.findByCourseCode(course.getCode());
            for (CourseSlot s : slots) {
                if (s.getSlotCode().replaceAll("\\s+", "").equalsIgnoreCase(target)) {
                    return s;
                }
            }
            if (!slots.isEmpty()) {
                return slots.get(0);
            }
        }
        return null;
    }

    @Transactional
    public List<MasterlistDTO> executeFcfsRegistration(String username) {
        // 1. Verify Registration Schedule Status
        RegistrationConfigDTO config = adminService.getConfigDTO();
        if (config == null || !"ACTIVE".equalsIgnoreCase(config.getStatus())) {
            String status = config != null ? config.getStatus() : "INACTIVE";
            String reason;
            if ("UPCOMING".equalsIgnoreCase(status)) {
                reason = "Course registration is scheduled to open on " + config.getFormattedStartDate() + ". Registrations cannot be accepted before the scheduled start time.";
            } else if ("CLOSED".equalsIgnoreCase(status)) {
                reason = "Course registration window closed on " + config.getFormattedEndDate() + ". Registrations can no longer be processed.";
            } else if ("PAUSED".equalsIgnoreCase(status)) {
                reason = "Course registration has been temporarily paused by the Administrator.";
            } else {
                reason = "No course registration schedule is currently active. Please contact administrator.";
            }
            throw new RuntimeException(reason);
        }

        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found: " + username));

        List<MasterlistEntry> entries = masterlistRepository.findByStudentUsernameOrderByRankNumberAsc(username);

        // Track occupied slot tokens for clash prevention
        List<Registration> currentRegs = registrationRepository.findByStudentUsernameAndStatus(username, "REGISTERED");
        Set<String> occupiedSlotTokens = new HashSet<>();
        for (Registration reg : currentRegs) {
            if (reg.getCourseSlot() != null) {
                occupiedSlotTokens.addAll(parseSlotTokens(reg.getCourseSlot().getSlotCode()));
            }
        }

        int allocatedCount = 0;
        double totalCreditsAllocated = 0.0;

        for (MasterlistEntry entry : entries) {
            Course course = entry.getCourse();
            boolean alreadyReg = registrationRepository.existsByStudentUsernameAndCourseCodeAndStatus(
                    student.getUsername(), course.getCode(), "REGISTERED");

            if (alreadyReg) {
                entry.setStatus("Already Registered ✓");
                masterlistRepository.save(entry);
                continue;
            }

            boolean allocated = false;
            String failReason = "No choices available";

            CourseSlot[] choices = new CourseSlot[]{entry.getChoice1(), entry.getChoice2(), entry.getChoice3()};
            int choiceIndex = 1;

            for (CourseSlot targetSlot : choices) {
                if (targetSlot == null) {
                    choiceIndex++;
                    continue;
                }

                if (targetSlot.getAvailableSeats() <= 0) {
                    failReason = "No Seats Available in Choice " + choiceIndex + " (" + targetSlot.getSlotCode() + " - " + (targetSlot.getFacultyName() != null ? targetSlot.getFacultyName() : "Faculty") + ")";
                    choiceIndex++;
                    continue;
                }

                if (hasSlotClash(targetSlot.getSlotCode(), occupiedSlotTokens)) {
                    failReason = "Slot Clash in Choice " + choiceIndex + " (" + targetSlot.getSlotCode() + ") with previously registered course schedule";
                    choiceIndex++;
                    continue;
                }

                try {
                    RegistrationRequest regReq = RegistrationRequest.builder()
                            .username(username)
                            .courseCode(course.getCode())
                            .theorySlotId("THEORY".equalsIgnoreCase(targetSlot.getSlotType()) ? targetSlot.getId() : null)
                            .labSlotId("LAB".equalsIgnoreCase(targetSlot.getSlotType()) ? targetSlot.getId() : null)
                            .build();

                    registrationService.registerCourse(regReq);
                    occupiedSlotTokens.addAll(parseSlotTokens(targetSlot.getSlotCode()));

                    String choiceLabel = choiceIndex == 1 ? "Choice 1 (Primary)" : ("Choice " + choiceIndex + " (Fallback)");
                    entry.setStatus("Allocated: " + choiceLabel + " ✓");
                    masterlistRepository.save(entry);
                    allocated = true;
                    allocatedCount++;
                    totalCreditsAllocated += course.getCredits();
                    break;
                } catch (Exception e) {
                    failReason = e.getMessage() != null ? e.getMessage() : "Slot Clash";
                }
                choiceIndex++;
            }

            if (!allocated) {
                entry.setStatus("Skipped: " + failReason);
                masterlistRepository.save(entry);

                // Generate specific notification for student with guidance
                String notifTitle = "Registration Alert: " + course.getCode() + " (" + course.getTitle() + ")";
                String notifBody;
                if (failReason.toLowerCase().contains("clash")) {
                    notifBody = "Slot Clash detected for " + course.getCode() + " - " + course.getTitle() + " (Rank #" + entry.getRankNumber() + "). All preferred choices clashed with your existing timetable slots. Please choose an alternative slot/faculty or select another course in Quick Register.";
                } else if (failReason.toLowerCase().contains("seat")) {
                    notifBody = "No Seats Available for " + course.getCode() + " - " + course.getTitle() + " (Rank #" + entry.getRankNumber() + "). Preferred faculty slots are currently full. Please choose another faculty slot or select an alternative course.";
                } else {
                    notifBody = "Could not allocate " + course.getCode() + " - " + course.getTitle() + " (Rank #" + entry.getRankNumber() + "). Reason: " + failReason + ". Please choose an alternative slot or course in Quick Register.";
                }

                notificationService.createNotification(student, notifTitle, "registration", "urgent", notifBody, "Modify Choices", "quick_register.html");
            }
        }

        if (allocatedCount > 0) {
            String notifTitle = "Course Registration Successful!";
            String notifBody = "Successfully registered " + allocatedCount + " course(s) (" + totalCreditsAllocated + " Credits) following your rank preferences. View your registered courses and weekly schedule.";
            notificationService.createNotification(student, notifTitle, "registration", "success", notifBody, "View Timetable", "timetable.html");
        }

        return getMasterlist(username);
    }

    private boolean hasSlotClash(String slotCode, Set<String> occupiedTokens) {
        if (slotCode == null || slotCode.isBlank()) return false;
        Set<String> tokens = parseSlotTokens(slotCode);
        for (String token : tokens) {
            if (occupiedTokens.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> parseSlotTokens(String slotCode) {
        Set<String> set = new HashSet<>();
        if (slotCode == null) return set;
        String[] parts = slotCode.split("[+\\s/,]+");
        for (String p : parts) {
            String clean = p.trim().toUpperCase();
            if (!clean.isEmpty()) {
                set.add(clean);
            }
        }
        return set;
    }

    @Transactional
    public void clearMasterlist(String username) {
        masterlistRepository.deleteByStudentUsername(username);
    }

    private MasterlistDTO toDTO(MasterlistEntry e) {
        return MasterlistDTO.builder()
                .id(e.getId())
                .rank(e.getRankNumber())
                .code(e.getCourse().getCode())
                .title(e.getCourse().getTitle())
                .category(e.getCourse().getBasket())
                .credits(e.getCourse().getCredits())
                .choice1(e.getChoice1() != null ? toChoiceDTO(e.getChoice1()) : null)
                .choice2(e.getChoice2() != null ? toChoiceDTO(e.getChoice2()) : null)
                .choice3(e.getChoice3() != null ? toChoiceDTO(e.getChoice3()) : null)
                .status(e.getStatus())
                .clash(false)
                .build();
    }

    private MasterlistDTO.SlotChoiceDTO toChoiceDTO(CourseSlot slot) {
        return MasterlistDTO.SlotChoiceDTO.builder()
                .slotId(slot.getId())
                .slot(slot.getSlotCode())
                .faculty(slot.getFacultyName())
                .venue(slot.getVenue())
                .seats(slot.getAvailableSeats())
                .maxSeats(slot.getTotalSeats())
                .build();
    }
}

