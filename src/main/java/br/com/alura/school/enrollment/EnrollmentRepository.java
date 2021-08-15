package br.com.alura.school.enrollment;

import br.com.alura.school.course.Course;
import br.com.alura.school.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserAndCourse(User user, Course course);

    @Query("SELECT NEW br.com.alura.school.enrollment.EnrollmentReportNode(u.email, COUNT(e.user) AS quantidade_matriculas)\n" +
            "FROM Enrollment e\n" +
            "INNER JOIN e.user u\n" +
            "GROUP BY u.email\n" +
            "ORDER BY quantidade_matriculas DESC")
    List<EnrollmentReportNode> getReport();

}
