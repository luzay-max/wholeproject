package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotBlank(message = "信息ID不能为空")
    @Size(max = 64, message = "信息ID长度不能超过64")
    private String infoId;

    @NotBlank(message = "信息类型不能为空")
    @Pattern(regexp = "(?i)^(lost|find)$", message = "信息类型仅支持lost或find")
    private String infoType;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;
}
