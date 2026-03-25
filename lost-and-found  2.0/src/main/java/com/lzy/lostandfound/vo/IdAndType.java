package com.lzy.lostandfound.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdAndType {
    @NotBlank(message = "id required")
    private String id;

    @NotBlank(message = "type required")
    @Pattern(regexp = "(?i)^(lost|find)$", message = "type must be lost or find")
    private String type;

    @Size(max = 200, message = "reason max 200")
    private String reason;
}
