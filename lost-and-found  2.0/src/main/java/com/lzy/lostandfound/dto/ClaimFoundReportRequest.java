package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClaimFoundReportRequest {
    @NotBlank(message = "失物信息ID不能为空")
    private String itemId;

    @Size(max = 200, message = "说明不能超过200字")
    private String note;
}

