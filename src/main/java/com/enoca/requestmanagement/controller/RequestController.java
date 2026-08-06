package com.enoca.requestmanagement.controller;

import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.RequestResponse;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.security.CustomUserDetails;
import com.enoca.requestmanagement.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Talepler", description = "Talep olusturma ve yonetimi")
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    @Operation(summary = "Talep tipine gore yeni bir talep olusturur (taslak)")
    public ResponseEntity<RequestResponse> create(@Valid @RequestBody CreateRequestDto dto,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestService.create(dto, principal.getUser()));
    }

    @GetMapping
    @Operation(summary = "Oturum acan kullanicinin kendi taleplerini listeler")
    public ResponseEntity<PageResponse<RequestResponse>> listMyRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestType requestType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                requestService.findMyRequests(principal.getUser(), status, requestType, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Talep detayini ve onay gecmisini getirir")
    public ResponseEntity<RequestResponse> getById(@PathVariable Long id,
                                                   @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(requestService.findById(id, principal.getUser()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Taslak durumundaki bir talebi gunceller")
    public ResponseEntity<RequestResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody CreateRequestDto dto,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(requestService.update(id, dto, principal.getUser()));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Talebi onaya gonderir ve talep tipine gore onay zincirini olusturur")
    public ResponseEntity<RequestResponse> submit(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(requestService.submit(id, principal.getUser()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Talebi iptal eder")
    public ResponseEntity<RequestResponse> cancel(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(requestService.cancel(id, principal.getUser()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Taslak durumundaki bir talebi siler")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails principal) {
        requestService.delete(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
