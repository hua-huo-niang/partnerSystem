package com.qiang.domain.request.team;

import com.qiang.comment.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamListRequest extends Page {
    /**
     * id
     */
    private Long id;
    /**
     * 队伍的名称
     */
    private String name;
    /**
     * 队伍的描述信息
     */
    private String description;
    /**
     * 队伍的人数上限
     */
    private Integer maxNum;
    /**
     * 队伍的队长用户id
     */
    private Long userId;
    /**
     * 队伍的状态：公开、私密、加密
     */
    private Integer status;
    /**
     * 队伍的过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 关键词搜索
     */
    private String searchText;

    /**
     * pageNum
     */

    /**
     * pageSize
     */


}
