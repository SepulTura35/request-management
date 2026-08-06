package com.enoca.requestmanagement.dto.response;

import com.enoca.requestmanagement.entity.RequestComment;
import com.enoca.requestmanagement.enums.Role;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Long authorId,
        String authorName,
        Role authorRole,
        LocalDateTime createdAt
) {

    public static CommentResponse from(RequestComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getUser().getFullName(),
                comment.getUser().getRole(),
                comment.getCreatedAt());
    }
}
