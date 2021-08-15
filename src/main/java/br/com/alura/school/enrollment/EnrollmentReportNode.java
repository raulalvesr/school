package br.com.alura.school.enrollment;

import com.fasterxml.jackson.annotation.JsonProperty;

class EnrollmentReportNode {

    @JsonProperty
    private final String email;

    @JsonProperty
    private final long quantidade_matriculas;

    public EnrollmentReportNode(String email, long quantidade_matriculas) {
        this.email = email;
        this.quantidade_matriculas = quantidade_matriculas;
    }

}
