package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LostInfoUpdateRequest {
    @NotBlank(message = "id required")
    private String id;

    @NotBlank(message = "name required")
    @Size(max = 50, message = "name max 50")
    private String name;

    @NotBlank(message = "type required")
    @Size(max = 20, message = "type max 20")
    private String type;

    @NotBlank(message = "location required")
    @Size(max = 100, message = "location max 100")
    private String location;

    @NotBlank(message = "description required")
    @Size(min = 5, max = 500, message = "description 5-500")
    private String description;

    @Size(max = 2000, message = "images max 2000")
    private String images;

    @Size(max = 20, message = "contactName max 20")
    private String contactName;

    @NotBlank(message = "contactPhone required")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "invalid phone")
    private String contactPhone;

    @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "invalid email")
    @Size(max = 100, message = "contactEmail max 100")
    private String contactEmail;

    @NotBlank(message = "lostTime required")
    @Pattern(regexp = "^\\d{13}$", message = "lostTime must be 13-digit timestamp")
    private String lostTime;
}
