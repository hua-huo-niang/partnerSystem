package com.qiang.comment.exception;

import com.qiang.comment.ErrorCode;
import lombok.Data;

@Data
public class SystemException extends RuntimeException{
    private ErrorCode errorCode;
    private String description;

    public SystemException(ErrorCode errorCode, String description) {
        super(errorCode.getMessgae());
        this. errorCode = errorCode;
        this.description = description;
    }
}
