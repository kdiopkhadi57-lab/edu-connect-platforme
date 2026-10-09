package com.educonnect.web.dto;

import com.educonnect.domain.User;

public record UserSummaryDto(Long id, String fullName, String roleLabel, String institution,
                             String levelLabel, String fieldName) {

    public static UserSummaryDto of(User u) {
        return new UserSummaryDto(u.getId(), u.getFullName(), u.getRole().getLabel(), u.getInstitution(),
                u.getLevel() == null ? null : u.getLevel().getLabel(),
                u.getField() == null ? null : u.getField().getName());
    }
}
