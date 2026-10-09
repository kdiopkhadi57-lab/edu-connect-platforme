package com.educonnect.web.dto;

import com.educonnect.domain.Level;
import com.educonnect.domain.PostType;

import java.time.Instant;
import java.util.List;

public record PostDto(Long id, PostType type, String typeLabel, String title, String content,
                      boolean hasSolution, String solution,
                      FieldDto field, Level level, String levelLabel, String serie,
                      Long communityId, String communityName,
                      String videoUrl, List<String> tags, List<AttachmentDto> attachments,
                      UserSummaryDto author, Instant createdAt,
                      int likeCount, int commentCount, int downloadCount,
                      boolean likedByMe, boolean bookmarkedByMe, boolean mine) {
}
