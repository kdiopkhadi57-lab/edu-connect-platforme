package com.educonnect.web.dto;

import com.educonnect.domain.Field;

public record FieldDto(Long id, String name, String icon) {

    public static FieldDto of(Field f) {
        return f == null ? null : new FieldDto(f.getId(), f.getName(), f.getIcon());
    }
}
