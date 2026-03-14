package com.company.hrms.platform.starter.error.exception;

import org.springframework.http.HttpStatus;

public class HrmsException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public HrmsException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
