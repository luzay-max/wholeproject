package com.lzy.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("honor_period")
public class HonorPeriod {

    @TableId
    private String id;

    private String periodType;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private Integer topN;

    private Integer totalCompletedCount;

    private String status;

    private LocalDateTime sendTime;

    private LocalDateTime awardTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}

