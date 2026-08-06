package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.dto.request.CreateCommentRequest;
import com.enoca.requestmanagement.dto.response.CommentResponse;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.RequestComment;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.AuditAction;
import com.enoca.requestmanagement.event.AuditEvent;
import com.enoca.requestmanagement.exception.ResourceNotFoundException;
import com.enoca.requestmanagement.repository.RequestCommentRepository;
import com.enoca.requestmanagement.repository.RequestRepository;
import com.enoca.requestmanagement.security.RequestAccessPolicy;
import com.enoca.requestmanagement.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final RequestCommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final RequestAccessPolicy accessPolicy;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponse addComment(Long requestId, CreateCommentRequest request, User author) {
        Request target = loadVisibleRequest(requestId, author);

        RequestComment comment = RequestComment.builder()
                .request(target)
                .user(author)
                .content(request.content())
                .build();

        RequestComment saved = commentRepository.save(comment);

        eventPublisher.publishEvent(AuditEvent.of(
                AuditAction.COMMENT_ADDED, "Request", target.getId(), author.getId(),
                "Talebe yorum eklendi: " + target.getRequestNumber()));

        return CommentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findByRequest(Long requestId, User viewer) {
        Request target = loadVisibleRequest(requestId, viewer);

        return commentRepository.findByRequestWithUser(target).stream()
                .map(CommentResponse::from)
                .toList();
    }

    private Request loadVisibleRequest(Long requestId, User user) {
        Request request = requestRepository.findByIdWithRequester(requestId)
                .orElseThrow(() -> ResourceNotFoundException.of("Talep", requestId));

        accessPolicy.ensureCanView(request, user);

        return request;
    }
}
