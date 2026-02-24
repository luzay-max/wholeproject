package com.lzy.lostandfound.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class WhitelistImportDTO {
    @ExcelProperty("学号")
    private String studentId;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("学院")
    private String college;
}
