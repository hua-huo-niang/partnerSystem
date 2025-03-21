package com.qiang.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResultDTO {
    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 每页显示的条数
     */
    private Integer pageSize;
    /**
     * 当前页显示的真实条数
     */
    private Integer size;
    /**
     * 记录数
     */
    private Object records;
}
