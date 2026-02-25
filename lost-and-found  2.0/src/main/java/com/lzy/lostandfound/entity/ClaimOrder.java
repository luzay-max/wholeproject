package com.lzy.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("claim_order")
public class ClaimOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String itemType;
    private String itemId;
    private String itemName;
    private String publisherUserId;
    private String applicantUserId;
    private String applyNote;
    private String proofText;
    private String proofImages;
    private String status;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime proofSubmitTime;
    private LocalDateTime confirmTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

