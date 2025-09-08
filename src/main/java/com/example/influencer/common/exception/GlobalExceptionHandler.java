package com.example.influencer.common.exception;

import com.example.influencer.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;



@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    /* ==== 400: 잘못된 요청 ==== */

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.debug("400 IllegalArgument: {}", rootMessage(e));
        return ApiResponse.error("BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult()
                .getFieldErrors()
                .stream().findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("유효하지 않은 요청입니다.");
        log.debug("400 MethodArgumentNotValid: {}", msg);
        return ApiResponse.error("VALIDATION_ERROR", msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations()
                .stream().findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("유효하지 않은 요청입니다.");
        log.debug("400 ConstraintViolation: {}", msg);
        return ApiResponse.error("VALIDATION_ERROR", msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.debug("400 NotReadable: {}", rootMessage(e));
        return ApiResponse.error("VALIDATION_ERROR", "요청 본문(JSON) 형식이 잘못되었습니다.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = String.format("파라미터 '%s'의 타입이 올바르지 않습니다.", e.getName());
        log.debug("400 TypeMismatch: {}", rootMessage(e));
        return ApiResponse.error("VALIDATION_ERROR", msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        log.debug("400 MissingParam: {}", msg);
        return ApiResponse.error("VALIDATION_ERROR", msg);
    }

    //트랜잭션 내부 Bean Validation 실패
    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTxValidation(TransactionSystemException e) {
        ConstraintViolationException cve = findCause(e, ConstraintViolationException.class);
        if (cve != null) {
            String msg = cve.getConstraintViolations()
                    .stream().findFirst()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .orElse("유효하지 않은 요청입니다.");
            log.debug("400 Tx ConstraintViolation: {}", msg);
            return ApiResponse.error("VALIDATION_ERROR", msg);
        }
        log.error("400 Transaction error (non-validation): {}", rootMessage(e));
        return ApiResponse.error("BAD_REQUEST", "요청 처리 중 오류가 발생했습니다.");
    }

    //401 403

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuth(AuthenticationException e) {
        log.debug("401 Unauthorized: {}", rootMessage(e));
        return ApiResponse.error("UNAUTHORIZED", "인증이 필요합니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccess(AccessDeniedException e) {
        log.debug("403 Forbidden: {}", rootMessage(e));
        return ApiResponse.error("FORBIDDEN", "접근 권한이 없습니다.");
    }

    //404 405

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(NoHandlerFoundException e) {
        log.debug("404 NotFound: {}", e.getRequestURL());
        return ApiResponse.error("NOT_FOUND", "존재하지 않는 경로입니다.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.debug("405 MethodNotAllowed: {}", e.getMethod());
        return ApiResponse.error("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다.");
    }

    //409

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        String msg = simpleSqlMessage(e);
        log.debug("409 DataIntegrityViolation: {}", rootMessage(e));
        return ApiResponse.error("CONFLICT", msg);
    }

    //500

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleOthers(Exception e) {
        log.error("500 Unexpected error: {}", rootMessage(e), e);
        return ApiResponse.error("INTERNAL_SERVER_ERROR", "Unexpected error");
    }

    //Helpers

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        String name = cur.getClass().getSimpleName();
        String msg  = (cur.getMessage() != null) ? cur.getMessage() : t.toString();
        return name + ": " + msg;
    }

    private <T extends Throwable> T findCause(Throwable t, Class<T> type) {
        Throwable cur = t;
        while (cur != null) {
            if (type.isInstance(cur)) return type.cast(cur);
            cur = cur.getCause();
        }
        return null;
    }

    private String simpleSqlMessage(Throwable t) {
        String m = rootMessage(t);
        if (m == null) return "데이터 무결성 위반";
        String lower = m.toLowerCase();
        if (lower.contains("duplicate") || lower.contains("unique")) return "중복된 데이터가 존재합니다.";
        if (lower.contains("foreign key")) return "연관 데이터 제약조건을 위반했습니다.";
        return "데이터 무결성 위반";
    }
}
