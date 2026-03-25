package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClaimProofRequest {
    @NotBlank(message = "凭证说明不能为空")
    @Size(max = 500, message = "凭证说明不能超过500字")
    private String proofText;

    @Size(max = 2000, message = "凭证图片字段长度不能超过2000")
    private String proofImages;
}

