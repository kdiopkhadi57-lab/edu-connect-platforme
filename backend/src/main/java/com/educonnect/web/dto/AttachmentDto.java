package com.educonnect.web.dto;

import com.educonnect.domain.Attachment;

public record AttachmentDto(Long id, String name, String contentType, long size, String url) {

    public static AttachmentDto of(Attachment a) {
        return new AttachmentDto(a.getId(), a.getOriginalName(), a.getContentType(), a.getSize(),
                "/api/files/" + a.getStoredName());
    }
}
