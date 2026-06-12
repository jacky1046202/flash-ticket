package com.example.flashticket.exception;

import com.example.flashticket.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(TicketNotFoundException e) {
        return ApiResponse.fail(404, e.getMessage());
    }

    @ExceptionHandler({SoldOutException.class, DuplicateOrderException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleConflict(BusinessException e) {
        return ApiResponse.fail(409, e.getMessage());
    }

    @ExceptionHandler(CampaignNotActiveException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleNotActive(CampaignNotActiveException e) {
        return ApiResponse.fail(403, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(MissingServletRequestParameterException e) {
        return ApiResponse.fail(400, "缺少參數:" + e.getParameterName());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnexpected(Exception e) {
        log.error("未預期的錯誤", e);
        return ApiResponse.fail(500, "系統繁忙, 請稍後再試");
    }
}
