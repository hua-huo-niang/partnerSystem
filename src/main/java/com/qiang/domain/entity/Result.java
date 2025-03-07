package com.qiang.domain.entity;

import com.qiang.util.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.sql.rowset.serial.SerialRef;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code;
    private Object data;
    private String errorMsg;
    private String description;

    static public Result ok(ErrorCode errorCode){
        return new Result(errorCode.getCode(),null,null,null);
    }
    static public Result ok(ErrorCode errorCode,Object data){
        return new Result(errorCode.getCode(),data,null,null);
    }
    static public Result ok(ErrorCode errorCode,List<?> data){
        return new Result(errorCode.getCode(),data,null,null);
    }

    static public Result fail(ErrorCode errorCode,String description){
        return new Result(errorCode.getCode(),null, errorCode.getMessgae(), description);
    }
    static public Result fail(ErrorCode errorCode){
        return new Result(errorCode.getCode(),null,errorCode.getMessgae(),null);
    }

    static public Result fail(Integer code,String errorMsg,String description){
        return new Result(code,null,errorMsg,description);
    }

}
