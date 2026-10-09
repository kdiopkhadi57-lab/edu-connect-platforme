package com.educonnect.web.dto;

import com.educonnect.domain.Level;
import com.educonnect.domain.Role;
import com.educonnect.domain.User;

import java.time.Instant;

public record UserDto(Long id, String fullName, String email, Role role, String roleLabel,
                      String institution, FieldDto field, Level level, String levelLabel, String serie,
                      String bio, Instant createdAt,
                      long postCount, long followerCount, long followingCount, boolean followedByMe) {

    public static UserDto of(User u, boolean withEmail, long posts, long followers, long following, boolean followedByMe) {
        return new UserDto(u.getId(), u.getFullName(), withEmail ? u.getEmail() : null, u.getRole(), u.getRole().getLabel(),
                u.getInstitution(), FieldDto.of(u.getField()), u.getLevel(),
                u.getLevel() == null ? null : u.getLevel().getLabel(), u.getSerie(), u.getBio(), u.getCreatedAt(),
                posts, followers, following, followedByMe);
    }
}
