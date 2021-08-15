package br.com.alura.school.enrollment;

import br.com.alura.school.course.Course;
import br.com.alura.school.course.CourseRepository;
import br.com.alura.school.user.User;
import br.com.alura.school.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
class EnrollmentController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    EnrollmentController(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/courses/{courseCode}/enroll")
    ResponseEntity<Void> newEnrollment(@PathVariable("courseCode") String courseCode, @RequestBody @Valid NewEnrollmentRequest newEnrollmentRequest) {
        User user = userRepository.findByUsername(newEnrollmentRequest.getUsername())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format("User with username \"%s\" not found", newEnrollmentRequest.getUsername())));

        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format("Course with code \"%s\" not found", courseCode)));

        boolean userIsAlreadyEnrolledInCourse = enrollmentRepository.existsByUserAndCourse(user, course);

        if (userIsAlreadyEnrolledInCourse)
            throw new ResponseStatusException(BAD_REQUEST, "User is already enrolled in this course");

        enrollmentRepository.save(new Enrollment(user, course));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("courses/enroll/report")
    ResponseEntity<List<EnrollmentReportNode>> getEnrollmentReport() {
        List<EnrollmentReportNode> enrollmentReport = enrollmentRepository.getReport();

        if (enrollmentReport.isEmpty())
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(enrollmentReport);
    }

}
