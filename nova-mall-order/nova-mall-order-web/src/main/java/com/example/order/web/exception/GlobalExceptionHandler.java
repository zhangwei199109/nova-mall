package com.example.order.web.exception;

import com.example.common.dto.Result;
import com.example.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidate(Exception e) {
        FieldError fieldError = null;
        if (e instanceof MethodArgumentNotValidException manve) {
            fieldError = manve.getBindingResult().getFieldError();
        } else if (e instanceof BindException be) {
            fieldError = be.getBindingResult().getFieldError();
        }
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("参数校验异常: {}", msg);
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Object handleOther(HttpServletRequest request, Exception e) {
        String ct = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");
        boolean isEventStream = (ct != null && ct.contains("text/event-stream")) || (accept != null && accept.contains("text/event-stream"));
        if (isEventStream) {
            // 对 SSE/流式请求，仅记录，不再返回 Result，避免转换器错误
            log.warn("SSE/stream request error (suppressed): {}", e.toString());
            return null;
        }
        log.error("系统异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}



