package com.educonnect.web.dto;

import com.educonnect.domain.Level;
import com.educonnect.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Role role,
        @Size(max = 150) String institution,
        @NotNull Long fieldId,
        @NotNull Level level,
        @Size(max = 20) String serie) {
}
