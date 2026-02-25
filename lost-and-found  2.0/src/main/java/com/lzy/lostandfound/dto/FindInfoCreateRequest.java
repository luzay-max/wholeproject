package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FindInfoCreateRequest {
    @NotBlank(message = "物品名称不能为空")
    @Size(max = 50, message = "物品名称不能超过50个字符")
    private String name;

    @NotBlank(message = "物品类型不能为空")
    @Size(max = 20, message = "物品类型不能超过20个字符")
    private String type;

    @NotBlank(message = "拾取地点不能为空")
    @Size(max = 100, message = "拾取地点不能超过100个字符")
    private String location;

    @NotBlank(message = "物品描述不能为空")
    @Size(min = 5, max = 500, message = "物品描述长度需在5到500个字符之间")
    private String description;

    @Size(max = 2000, message = "图片字段长度不能超过2000个字符")
    private String images;

    @Size(max = 20, message = "联系人姓名不能超过20个字符")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "联系邮箱格式不正确")
    @Size(max = 100, message = "联系邮箱不能超过100个字符")
    private String contactEmail;

    @Pattern(regexp = "^$|^\\d{13}$", message = "拾取时间格式不正确")
    private String foundTime;
}
