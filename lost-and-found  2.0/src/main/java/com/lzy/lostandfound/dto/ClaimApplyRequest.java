package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClaimApplyRequest {
    @NotBlank(message = "信息类型不能为空")
    @Pattern(regexp = "(?i)^(lost|find)$", message = "信息类型必须为 lost 或 find")
    private String itemType;

    @NotBlank(message = "信息ID不能为空")
    private String itemId;

    @Size(max = 200, message = "申请说明不能超过200字")
    private String applyNote;
}

