package com.lzy.lostandfound.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HonorBoardVO {

    private String periodId;

    private String periodType;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private Integer topN;

    private Integer totalCompletedCount;

    private String status;

    private List<HonorBoardItemVO> list;
}

