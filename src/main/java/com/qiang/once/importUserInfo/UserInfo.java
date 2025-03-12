package com.qiang.once.importUserInfo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserInfo {
    /**
     * 用户id
     */
    @ExcelProperty("成员编号")
    private Long id;
    /**
     * 用户名称
     */
    @ExcelProperty("成员昵称")
    private String username;
}
