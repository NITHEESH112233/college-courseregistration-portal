package course.registration.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    public enum BasketCategory {
        UC,
        UE,
        PC,
        PE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String courseCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private BasketCategory basketCategory;

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false, length = 100)
    private String courseType;

    /*
     * Required because GET /api/courses is supposed to
     * return only active courses.
     */
    @Column(nullable = false)
    private boolean active = true;

    public Course() {
    }

    public Course(
            String courseCode,
            String title,
            BasketCategory basketCategory,
            Integer credits,
            String courseType
    ) {
        this.courseCode = courseCode;
        this.title = title;
        this.basketCategory = basketCategory;
        this.credits = credits;
        this.courseType = courseType;
        this.active = true;
    }

    public Course(
            String courseCode,
            String title,
            BasketCategory basketCategory,
            Integer credits,
            String courseType,
            boolean active
    ) {
        this.courseCode = courseCode;
        this.title = title;
        this.basketCategory = basketCategory;
        this.credits = credits;
        this.courseType = courseType;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BasketCategory getBasketCategory() {
        return basketCategory;
    }

    public void setBasketCategory(BasketCategory basketCategory) {
        this.basketCategory = basketCategory;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}