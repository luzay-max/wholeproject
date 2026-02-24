package com.lzy.lostandfound.exception;

import com.lzy.lostandfound.vo.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {//全局异常处理器
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String message = error != null ? error.getDefaultMessage() : "参数错误";
        return Result.error(message);
    }

    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String message = error != null ? error.getDefaultMessage() : "参数错误";
        return Result.error(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("参数错误");
        return Result.error(message);
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        e.printStackTrace();
        return Result.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "操作失败");//判断是否为空 给错误信息
        //传参 到Result 中 最好判断 错误信息是否给来源 用Spring 的 StringUtils 工具 判断是否为空

    }
}
