package com.lzy.lostandfound.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchCandidateVO {
    private String id;
    private String itemType;
    private String name;
    private String type;
    private String location;
    private String description;
    private String images;
    private LocalDateTime timeValue;
    private LocalDateTime publishTime;
    private Integer matchScore;
    private String matchReason;
}

