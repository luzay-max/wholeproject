package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 3, max = 20, message = "username 3-20")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "invalid username")
    private String username;

    @Size(min = 2, max = 20, message = "name 2-20")
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "invalid phone")
    private String phone;

    @Email(message = "invalid email")
    @Size(max = 100, message = "email max 100")
    private String email;

    @Size(min = 1, max = 50, message = "account name 1-50")
    private String accountName;
}
