package br.com.alura.school.enrollment;

import br.com.alura.school.course.*;
import br.com.alura.school.user.User;
import br.com.alura.school.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EnrollmentControllerTest {

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    void should_add_new_enrollment() throws Exception {
        User johnUser = userRepository.save(new User("john", "john@gmail.com"));
        courseRepository.save(new Course("java-1", "Java 1", "Basic Java"));

        NewEnrollmentRequest newEnrollment = new NewEnrollmentRequest(johnUser.getUsername());

        mockMvc.perform(post("/courses/java-1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newEnrollment)))
                        .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"\t", "\n", "an-username-that-is-really-really-big"})
    void should_validate_bad_enrollment_requests(String username) throws Exception {
        courseRepository.save(new Course("java-1", "Java 1", "Basic Java"));

        NewEnrollmentRequest newEnrollment = new NewEnrollmentRequest(username);

        mockMvc.perform(post("/courses/java-1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newEnrollment)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void should_not_allow_duplication_of_enrollment() throws Exception {
        User johnUser = userRepository.save(new User("john", "john@gmail.com"));
        Course javaCourse = courseRepository.save(new Course("java-1", "Java 1", "Basic Java"));
        enrollmentRepository.save(new Enrollment(johnUser, javaCourse));

        NewEnrollmentRequest newEnrollment = new NewEnrollmentRequest(johnUser.getUsername());

        mockMvc.perform(post("/courses/java-1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newEnrollment)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void should_retrieve_enrollment_report() throws Exception {
        User janeUser = userRepository.save(new User("jane", "jane@gmail.com"));
        User johnUser = userRepository.save(new User("john", "john@gmail.com"));

        Course javaCourse = courseRepository.save(new Course("java-1", "Java 1", "Basic Java"));
        Course cppCourse = courseRepository.save(new Course("cpp-1", "C++ 1", "Basic C++"));

        enrollmentRepository.save(new Enrollment(janeUser, javaCourse));
        enrollmentRepository.save(new Enrollment(johnUser, cppCourse));
        enrollmentRepository.save(new Enrollment(johnUser, javaCourse));

        mockMvc.perform(get("/courses/enroll/report")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.length()", is(2)))
                        .andExpect(jsonPath("$[0].email", is("john@gmail.com")))
                        .andExpect(jsonPath("$[0].quantidade_matriculas", is(2)))
                        .andExpect(jsonPath("$[1].email", is("jane@gmail.com")))
                        .andExpect(jsonPath("$[1].quantidade_matriculas", is(1)));
    }

    @Test
    void no_content_when_enrollment_report_is_empty() throws Exception {
        mockMvc.perform(get("/courses/enroll/report")
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNoContent());
    }

}