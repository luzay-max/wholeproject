package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotBlank(message = "infoId required")
    @Size(max = 64, message = "infoId max 64")
    private String infoId;

    @NotBlank(message = "infoType required")
    @Pattern(regexp = "(?i)^(lost|find)$", message = "infoType must be lost or find")
    private String infoType;

    @NotBlank(message = "content required")
    @Size(max = 500, message = "content max 500")
    private String content;
}
