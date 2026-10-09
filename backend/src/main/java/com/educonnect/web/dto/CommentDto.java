package com.educonnect.web.dto;

import com.educonnect.domain.Comment;

import java.time.Instant;

public record CommentDto(Long id, String content, UserSummaryDto author, Instant createdAt, boolean mine) {

    public static CommentDto of(Comment c, Long me) {
        return new CommentDto(c.getId(), c.getContent(), UserSummaryDto.of(c.getAuthor()), c.getCreatedAt(),
                c.getAuthor().getId().equals(me));
    }
}
