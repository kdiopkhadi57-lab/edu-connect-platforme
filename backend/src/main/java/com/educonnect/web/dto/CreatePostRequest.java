package com.educonnect.web.dto;

import com.educonnect.domain.Level;
import com.educonnect.domain.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
        @NotNull PostType type,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 20000) String content,
        @Size(max = 20000) String solution,
        Long fieldId,
        Level level,
        @Size(max = 20) String serie,
        Long communityId,
        @Size(max = 500) String videoUrl,
        List<@Size(max = 40) String> tags) {
}
