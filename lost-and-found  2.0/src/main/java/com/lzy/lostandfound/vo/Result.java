package com.lzy.lostandfound.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//统一响应结果
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private Integer code; // 业务状态码：0-成功，1-通用错误，401-未登录，403-权限不足
    private String message; // 提示信息
    private T data; // 响应数据

    // 快速返回操作成功响应结果(带响应数据)
    public static <E> Result<E> success(E data) {
        return new Result<>(0, "操作成功", data);
    }

    // 快速返回操作成功响应结果(无数据)
    public static Result success() {
        return new Result<>(0, "操作成功", null);
    }

    // 通用错误（如参数错误、用户名已存在）
    public static Result error(String message) {
        return new Result<>(1, message, null);
    }

    // 未登录（与前端拦截器的 401 处理对应）
    public static Result notLogin(String message) {
        return new Result<>(401, message, null);
    }

    // 权限不足（与前端拦截器的 403 处理对应）
    public static Result forbidden(String message) {
        return new Result<>(403, message, null);
    }
}