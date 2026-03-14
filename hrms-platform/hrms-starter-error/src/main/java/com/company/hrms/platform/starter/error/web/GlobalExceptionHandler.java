package com.company.hrms.platform.starter.error.web;

import com.company.hrms.platform.sharedkernel.web.ExchangeAttributeKeys;
import com.company.hrms.platform.starter.error.api.ApiErrorResponse;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HrmsException.class)
    public ResponseEntity<ApiErrorResponse> handleHrmsException(HrmsException ex, ServerWebExchange exchange) {
        return buildResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), exchange, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, ServerWebExchange exchange) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), exchange, List.of());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Throwable ex, ServerWebExchange exchange) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                exchange,
                List.of(ex.getClass().getSimpleName()));
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            ServerWebExchange exchange,
            List<String> details
    ) {
        Object correlation = exchange.getAttribute(ExchangeAttributeKeys.CORRELATION_ID);
        String correlationId = correlation == null ? null : correlation.toString();
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                errorCode,
                message,
                exchange.getRequest().getPath().value(),
                correlationId,
                details);

        return ResponseEntity.status(status).body(body);
    }
}
