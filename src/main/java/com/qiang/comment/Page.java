package com.qiang.comment;

import lombok.Data;

@Data
public class Page {
    /**
     * 当前页
     */
    private Integer pageNum;
    /**
     * 每页的页数
     */
    private Integer pageSize;
}
