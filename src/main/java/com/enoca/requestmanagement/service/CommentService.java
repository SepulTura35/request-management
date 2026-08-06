package com.enoca.requestmanagement.service;

import com.enoca.requestmanagement.dto.request.CreateCommentRequest;
import com.enoca.requestmanagement.dto.response.CommentResponse;
import com.enoca.requestmanagement.entity.User;

import java.util.List;

public interface CommentService {

    CommentResponse addComment(Long requestId, CreateCommentRequest request, User author);

    List<CommentResponse> findByRequest(Long requestId, User viewer);
}
