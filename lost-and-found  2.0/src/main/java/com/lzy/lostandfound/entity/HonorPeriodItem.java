package com.lzy.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("honor_period_item")
public class HonorPeriodItem {

    @TableId
    private String id;

    private String periodId;

    private String userId;

    @TableField("`rank`")
    private Integer rank;

    private Integer completedCount;

    private Integer points;

    private LocalDateTime lastCompletedAt;

    private String username;

    private String name;

    private String className;

    private String departmentName;

    private String avatar;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}

