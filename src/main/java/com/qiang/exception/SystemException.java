package com.qiang.exception;

import com.qiang.util.ErrorCode;
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
