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
public class Status {

    @NotBlank(message = "id required")
    private String id;

    @NotBlank(message = "status required")
    @Pattern(regexp = "(?i)^(PENDING|APPROVED|REJECTED|SOLVED)$", message = "invalid status")
    private String status;

    @Size(max = 200, message = "reason max 200")
    private String reason;

}
