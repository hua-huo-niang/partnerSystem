package com.qiang.exception;

import com.qiang.domain.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 处理系统异常
     * @param ex 系统异常
     * @return Result 统一响应对象
     */
    @ExceptionHandler(SystemException.class)
    public Result systemExceptionHandler(SystemException ex){
        //记录日志
        log.error("systemError-message:"+ex.getMessage());
        log.error("systemError-cause:"+ex.getCause());
        //发送消息给运维
        //发送邮件给开发人员，ex对象发送给开发人员
        return Result.fail(ex.getErrorCode(),ex.getDescription());
    }
    /**
     * 处理业务异常
     * @param ex 业务异常
     * @return Result 统一响应对象
     */
    @ExceptionHandler(BusinessException.class)
    public Result businessExceptionHandler(BusinessException ex){
        log.error("BusinessException-message"+ex.getMessage());
        log.error("BusinessException-cause:"+ex.getCause());
        return Result.fail(ex.getErrorCode(),ex.getDescription());
    }
    /**
     * 第三类异常，处理其他未意料的异常
     * @param ex 业务异常
     * @return Result 统一响应对象
     */
    @ExceptionHandler(Exception.class)
    public Result doOtherException(Exception ex){
        log.error("Exception-message:"+ex.getMessage());
        log.error("Exception-cause:"+ex.getCause());
        log.error("Exception-Class:"+ex.getClass());
        log.error("Exception-LocalizedMessage:"+ex.getLocalizedMessage());
        log.error("Exception-Suppressed:"+ex.getSuppressed());
        log.error("Exception-StackTrace:"+ex.getStackTrace());
        return Result.fail(500,"出异常了",null);
    }
}
