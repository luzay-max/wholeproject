package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "username required")
    @Size(min = 3, max = 20, message = "username 3-20")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "invalid username")
    private String username;

    @NotBlank(message = "password required")
    @Size(min = 6, max = 20, message = "password 6-20")
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$", message = "invalid password")
    private String password;

    @NotBlank(message = "confirmPassword required")
    private String confirmPassword;

    @NotBlank(message = "phone required")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "invalid phone")
    private String phone;

    @NotBlank(message = "email required")
    @Email(message = "invalid email")
    @Size(max = 100, message = "email max 100")
    private String email;

    @NotBlank(message = "name required")
    @Size(min = 2, max = 20, message = "name 2-20")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5\\u3400-\\u4db5a-zA-Z\\s.·]{2,20}$", message = "invalid name format")
    private String name;

    @NotBlank(message = "studentId required")
    @Pattern(regexp = "^[0-9A-Za-z]{4,20}$", message = "invalid studentId")
    private String studentId;

    @NotBlank(message = "role required")
    @Pattern(regexp = "(?i)^(STUDENT|TEACHER)$", message = "invalid role")
    private String role;
}
