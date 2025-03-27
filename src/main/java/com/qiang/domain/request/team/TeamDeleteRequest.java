package com.qiang.domain.request.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamDeleteRequest  implements Serializable {

    @Serial
    private static final long serialVersionUID = -2640474180570512196L;
    /**
     * 要删除的id
     */
    private Long teamId;
}
