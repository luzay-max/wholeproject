package com.lzy.lostandfound.vo;

import com.lzy.lostandfound.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tu {
    private String token;
    private User user;
    private String refreshToken;
}
