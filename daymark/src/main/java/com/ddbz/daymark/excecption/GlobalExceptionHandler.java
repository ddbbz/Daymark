package com.ddbz.daymark.excecption;

import com.ddbz.daymark.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusiness(BusinessException e){
        log.warn("业务异常:"+e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e){
        log.error("系统异常:",e);
        return Result.error("系统异常，请稍后重试");
    }
}
