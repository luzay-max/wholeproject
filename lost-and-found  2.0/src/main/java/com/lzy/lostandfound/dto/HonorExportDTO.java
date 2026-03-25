package com.lzy.lostandfound.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HonorExportDTO {

    @ExcelProperty("排名")
    private Integer rank;

    @ExcelProperty("用户ID")
    private String userId;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("部门")
    private String departmentName;

    @ExcelProperty("头像")
    private String avatar;

    @ExcelProperty("完成次数")
    private Integer completedCount;

    @ExcelProperty("积分")
    private Integer points;

    @ExcelProperty("最后完成时间")
    private String lastCompletedAt;
}
