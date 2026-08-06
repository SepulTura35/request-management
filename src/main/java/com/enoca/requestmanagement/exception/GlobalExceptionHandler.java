package com.enoca.requestmanagement.exception;

import com.enoca.requestmanagement.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "E-posta veya şifre hatalı", request.getRequestURI());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Hesabınız pasif durumda", request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz bulunmuyor", request.getRequestURI());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentModification(OptimisticLockingFailureException ex,
                                                                      HttpServletRequest request) {
        log.info("Eşzamanlı değişiklik reddedildi: {}", request.getRequestURI());
        return build(HttpStatus.CONFLICT,
                "Bu kayıt siz işlem yaparken başka bir kullanıcı tarafından güncellendi. "
                        + "Sayfayı yenileyip tekrar deneyin.",
                request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                      HttpServletRequest request) {
        log.warn("Veri butunlugu ihlali: {}", request.getRequestURI(), ex);
        return build(HttpStatus.CONFLICT,
                "İşlem, verinin mevcut durumuyla çakıştığı için tamamlanamadı. "
                        + "Sayfayı yenileyip tekrar deneyin.",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Beklenmeyen hata: {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu", request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse body = ErrorResponse.withValidationErrors(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Gönderilen veri doğrulama kurallarına uymuyor",
                pathOf(request),
                fieldErrors);

        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        ErrorResponse errorResponse = ErrorResponse.of(
                status.value(), status.getReasonPhrase(), ex.getMessage(), pathOf(request));

        return new ResponseEntity<>(errorResponse, headers, statusCode);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message, path));
    }

    private String pathOf(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : request.getDescription(false);
    }
}
