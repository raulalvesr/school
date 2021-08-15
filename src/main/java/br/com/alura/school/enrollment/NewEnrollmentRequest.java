package br.com.alura.school.enrollment;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

class NewEnrollmentRequest {

    @Size(max=20)
    @NotBlank
    @JsonProperty
    private String username;

    NewEnrollmentRequest() {}

    NewEnrollmentRequest(String username) {
        this.username = username;
    }

    String getUsername() {
        return username;
    }

}
