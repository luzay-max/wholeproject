package com.lzy.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//统计项目条数信息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Count {
    private  Long lostCount;//丢失数
    private  Long findCount;//寻找数
    private  Long rejectCount;//拒绝数
    private Long solvedCount;//解决数
    private  Long pendingCount;//审核数
    private Long userCount;//用户数
}
