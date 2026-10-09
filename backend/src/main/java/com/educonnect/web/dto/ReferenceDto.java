package com.educonnect.web.dto;

import java.util.List;

public record ReferenceDto(List<FieldDto> fields,
                           List<LabeledValue> levels,
                           List<String> series,
                           List<LabeledValue> roles,
                           List<LabeledValue> postTypes) {
}
