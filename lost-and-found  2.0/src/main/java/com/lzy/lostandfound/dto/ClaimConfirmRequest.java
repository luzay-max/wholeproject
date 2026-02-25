package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClaimConfirmRequest {
    @NotNull(message = "确认结果不能为空")
    private Boolean approved;

    @Size(max = 200, message = "驳回原因不能超过200字")
    private String rejectReason;
}

