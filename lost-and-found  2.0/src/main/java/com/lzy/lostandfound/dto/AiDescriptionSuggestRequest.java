package com.lzy.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AiDescriptionSuggestRequest {

    @NotBlank(message = "发布类型不能为空")
    @Pattern(regexp = "(?i)^(lost|find)$", message = "发布类型仅支持 lost 或 find")
    private String itemKind;

    @NotBlank(message = "物品名称不能为空")
    @Size(max = 50, message = "物品名称不能超过50个字符")
    private String name;

    @NotBlank(message = "物品类型不能为空")
    @Size(max = 32, message = "物品类型长度不能超过32个字符")
    private String type;

    @NotBlank(message = "地点不能为空")
    @Size(max = 100, message = "地点长度不能超过100个字符")
    private String location;

    @Size(max = 40, message = "时间参数长度不能超过40个字符")
    private String timeValue;

    @Size(max = 9, message = "图片数量不能超过9张")
    private List<@Size(max = 500, message = "图片地址长度不能超过500个字符") String> imageUrls;

    @Size(max = 500, message = "当前描述不能超过500个字符")
    private String currentDescription;

    @Pattern(regexp = "^$|(?i)^(objective_concise)$", message = "文案风格仅支持 objective_concise")
    private String style;
}

