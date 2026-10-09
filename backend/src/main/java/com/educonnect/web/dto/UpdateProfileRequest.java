package com.educonnect.web.dto;

import com.educonnect.domain.Level;
import com.educonnect.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotNull Role role,
        @Size(max = 150) String institution,
        @NotNull Long fieldId,
        @NotNull Level level,
        @Size(max = 20) String serie,
        @Size(max = 500) String bio) {
}
