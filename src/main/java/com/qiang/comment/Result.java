package com.qiang.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    /**
     * 自定义的响应码
     */
    private Integer code;
    /**
     * 响应的数据
     */
    private Object data;
    /**
     * 响应的错误信息的简单分类描述
     */
    private String errorMsg;
    /**
     * 错误信息的详细描述
     */
    private String description;
    static public Result ok(ErrorCode errorCode){
        return new Result(errorCode.getCode(),null,errorCode.getMessgae(),null);
    }
    static public Result ok(ErrorCode errorCode,Object data){
        return new Result(errorCode.getCode(),data,errorCode.getMessgae(),null);
    }
    static public Result ok(ErrorCode errorCode,Object data,String description){
        return new Result(errorCode.getCode(),data,errorCode.getMessgae(),description);
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
