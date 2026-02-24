package com.lzy.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_type")
public class SysDictType implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "dict_id", type = IdType.AUTO)
    private Long dictId;

    private String dictName;
    private String dictType;
    private String status;
    private String remark;
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
