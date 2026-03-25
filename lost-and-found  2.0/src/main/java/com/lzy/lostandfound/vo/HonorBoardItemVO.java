package com.lzy.lostandfound.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HonorBoardItemVO {

    private String id;

    private Integer rank;

    private String userId;

    private String username;

    private String name;

    private String className;

    private String departmentName;

    private String avatar;

    private Integer completedCount;

    private Integer points;

    private LocalDateTime lastCompletedAt;
}

