package com.enoca.requestmanagement.controller;

import com.enoca.requestmanagement.dto.request.ApproveRequest;
import com.enoca.requestmanagement.dto.request.RejectRequest;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.PendingApprovalResponse;
import com.enoca.requestmanagement.dto.response.RequestResponse;
import com.enoca.requestmanagement.security.CustomUserDetails;
import com.enoca.requestmanagement.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Onaylar", description = "Onay ve red islemleri")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    @Operation(summary = "Oturum acan kullaniciya atanmis, islem bekleyen onaylari listeler")
    public ResponseEntity<PageResponse<PendingApprovalResponse>> listPending(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(approvalService.findPending(principal.getUser(), pageable));
    }

    @GetMapping("/history")
    @Operation(summary = "Oturum acan kullanicinin gecmis onay aksiyonlarini listeler")
    public ResponseEntity<PageResponse<PendingApprovalResponse>> listHistory(
            @PageableDefault(size = 20, sort = "actionDate", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(approvalService.findHistory(principal.getUser(), pageable));
    }

    @PostMapping("/{stepId}/approve")
    @Operation(summary = "Onay adimini onaylar ve zinciri bir sonraki adima tasir")
    public ResponseEntity<RequestResponse> approve(@PathVariable Long stepId,
                                                   @Valid @RequestBody(required = false) ApproveRequest action,
                                                   @AuthenticationPrincipal CustomUserDetails principal) {
        ApproveRequest safeAction = action == null ? new ApproveRequest(null) : action;
        return ResponseEntity.ok(approvalService.approve(stepId, safeAction, principal.getUser()));
    }

    @PostMapping("/{stepId}/reject")
    @Operation(summary = "Onay adimini reddeder, aciklama zorunludur")
    public ResponseEntity<RequestResponse> reject(@PathVariable Long stepId,
                                                  @Valid @RequestBody RejectRequest action,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(approvalService.reject(stepId, action, principal.getUser()));
    }
}
