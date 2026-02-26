package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemReportRequest {
    @NotBlank(message = "信息ID不能为空")
    @Size(max = 64, message = "信息ID长度不能超过64")
    private String itemId;

    @NotBlank(message = "信息类型不能为空")
    @Pattern(regexp = "(?i)^(lost|find)$", message = "信息类型仅支持lost或find")
    private String itemType;

    @Size(max = 200, message = "举报说明不能超过200字")
    private String reason;
}

