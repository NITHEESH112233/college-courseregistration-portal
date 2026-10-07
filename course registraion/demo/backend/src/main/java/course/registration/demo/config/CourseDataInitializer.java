package course.registration.demo.config;

import course.registration.demo.entity.Course;
import course.registration.demo.repository.CourseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseDataInitializer {

    @Bean
    CommandLineRunner initializeCourses(CourseRepository courseRepository) {

        return args -> {

            if (courseRepository.count() > 0) {
                return;
            }

            courseRepository.save(new Course(
                    "CSE1001",
                    "Data Structures and Algorithms",
                    Course.BasketCategory.PC,
                    4,
                    "Program Core"
            ));

            courseRepository.save(new Course(
                    "MAT1011",
                    "Calculus for Engineers",
                    Course.BasketCategory.UC,
                    4,
                    "University Core"
            ));

            courseRepository.save(new Course(
                    "PHY1701",
                    "Engineering Physics",
                    Course.BasketCategory.UC,
                    3,
                    "University Core"
            ));

            courseRepository.save(new Course(
                    "CSE2001",
                    "Computer Architecture and Organization",
                    Course.BasketCategory.PC,
                    4,
                    "Program Core"
            ));

            courseRepository.save(new Course(
                    "CSE3002",
                    "Machine Learning",
                    Course.BasketCategory.PC,
                    4,
                    "Program Core"
            ));

            courseRepository.save(new Course(
                    "CSE2005",
                    "Database Management Systems",
                    Course.BasketCategory.PC,
                    4,
                    "Program Core"
            ));

            courseRepository.save(new Course(
                    "CSE3003",
                    "Computer Networks",
                    Course.BasketCategory.PC,
                    3,
                    "Program Core"
            ));

            courseRepository.save(new Course(
                    "CSE2505",
                    "Operating Systems",
                    Course.BasketCategory.PC,
                    4,
                    "Program Core"
            ));

            courseRepository.save(new Course(
                    "CSE2004",
                    "Software Engineering",
                    Course.BasketCategory.PE,
                    3,
                    "Program Elective"
            ));

            courseRepository.save(new Course(
                    "CSE3004",
                    "Intro to Artificial Intelligence",
                    Course.BasketCategory.PE,
                    3,
                    "Program Elective"
            ));

            courseRepository.save(new Course(
                    "CSE4011",
                    "Data Visualization",
                    Course.BasketCategory.PE,
                    3,
                    "Program Elective"
            ));

            courseRepository.save(new Course(
                    "SWE3007",
                    "Cloud Computing Architecture",
                    Course.BasketCategory.UE,
                    4,
                    "University Elective"
            ));

            courseRepository.save(new Course(
                    "PHY1001",
                    "Engineering Physics",
                    Course.BasketCategory.UC,
                    4,
                    "University Core"
            ));

            courseRepository.save(new Course(
                    "ECE1004",
                    "Signals and Systems",
                    Course.BasketCategory.PC,
                    3,
                    "Program Core"
            ));

            System.out.println("Course data initialized successfully.");
        };
    }
}