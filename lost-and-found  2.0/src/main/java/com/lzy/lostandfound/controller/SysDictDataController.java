package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.entity.SysDictData;
import com.lzy.lostandfound.service.ISysDictDataService;
import com.lzy.lostandfound.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典数据管理")
@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController {

    @Autowired
    private ISysDictDataService dictDataService;

    @Operation(summary = "查询字典数据列表")
    @GetMapping("/list")
    public Result<Page<SysDictData>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String dictType,
                                          @RequestParam(required = false) String dictLabel,
                                          @RequestParam(required = false) String status) {
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dictType), SysDictData::getDictType, dictType);
        wrapper.like(StringUtils.hasText(dictLabel), SysDictData::getDictLabel, dictLabel);
        wrapper.eq(StringUtils.hasText(status), SysDictData::getStatus, status);
        wrapper.orderByAsc(SysDictData::getDictSort);
        return Result.success(dictDataService.page(page, wrapper));
    }

    @Operation(summary = "根据字典类型查询字典数据信息")
    @GetMapping(value = "/type/{dictType}")
    public Result<List<SysDictData>> dictType(@PathVariable String dictType) {
        return Result.success(dictDataService.selectDictDataByType(dictType));
    }

    @Operation(summary = "获取字典数据详细信息")
    @GetMapping(value = "/{dictCode}")
    public Result<SysDictData> getInfo(@PathVariable Long dictCode) {
        return Result.success(dictDataService.getById(dictCode));
    }

    @Operation(summary = "新增字典数据")
    @Log("ADD_DICT_DATA")
    @PostMapping
    public Result add(@RequestBody SysDictData dict) {
        return Result.success(dictDataService.save(dict));
    }

    @Operation(summary = "修改字典数据")
    @Log("UPDATE_DICT_DATA")
    @PutMapping
    public Result edit(@RequestBody SysDictData dict) {
        return Result.success(dictDataService.updateById(dict));
    }

    @Operation(summary = "删除字典数据")
    @Log("DELETE_DICT_DATA")
    @DeleteMapping("/{dictCodes}")
    public Result remove(@PathVariable List<Long> dictCodes) {
        return Result.success(dictDataService.removeByIds(dictCodes));
    }
}
