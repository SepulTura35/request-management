package com.enoca.requestmanagement.controller;

import com.enoca.requestmanagement.dto.request.CreateCommentRequest;
import com.enoca.requestmanagement.dto.response.AuditLogResponse;
import com.enoca.requestmanagement.dto.response.CommentResponse;
import com.enoca.requestmanagement.security.CustomUserDetails;
import com.enoca.requestmanagement.service.AuditService;
import com.enoca.requestmanagement.service.CommentService;
import com.enoca.requestmanagement.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/requests/{requestId}")
@RequiredArgsConstructor
@Tag(name = "Talep Yorumlari", description = "Talep üzerindeki yorumlar ve işlem geçmişi")
public class RequestCommentController {

    private final CommentService commentService;
    private final AuditService auditService;
    private final RequestService requestService;

    @PostMapping("/comments")
    @Operation(summary = "Talebe yorum ekler")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long requestId,
                                                      @Valid @RequestBody CreateCommentRequest request,
                                                      @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(requestId, request, principal.getUser()));
    }

    @GetMapping("/comments")
    @Operation(summary = "Talebin yorumlarını getirir")
    public ResponseEntity<List<CommentResponse>> listComments(@PathVariable Long requestId,
                                                              @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(commentService.findByRequest(requestId, principal.getUser()));
    }

    @GetMapping("/audit")
    @Operation(summary = "Talep üzerinde yapılan tüm işlemlerin kaydını getirir")
    public ResponseEntity<List<AuditLogResponse>> listAudit(@PathVariable Long requestId,
                                                            @AuthenticationPrincipal CustomUserDetails principal) {
        requestService.findById(requestId, principal.getUser());
        return ResponseEntity.ok(auditService.findForEntity("Request", requestId));
    }
}
