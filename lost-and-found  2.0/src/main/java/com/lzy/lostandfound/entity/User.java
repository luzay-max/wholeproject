package com.lzy.lostandfound.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lzy.lostandfound.anno.State;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */

    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账户名
     */
    private String accountName;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色（STUDENT/TEACHER/ADMIN）
     */
    @State(message = "invalid role")
    private String role;

    /**
     * 学院
     */
    private String college;

    /**
     * 用户状态 (0 正常, 1 封禁)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

}
