package com.educonnect.web.dto;

import com.educonnect.domain.Community;
import com.educonnect.domain.CommunityType;

public record CommunityDto(Long id, String name, String description, CommunityType type,
                           long memberCount, boolean member) {

    public static CommunityDto of(Community c, long memberCount, boolean member) {
        return new CommunityDto(c.getId(), c.getName(), c.getDescription(), c.getType(), memberCount, member);
    }
}
