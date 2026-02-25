package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.entity.SysDictType;
import com.lzy.lostandfound.service.ISysDictTypeService;
import com.lzy.lostandfound.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典类型管理")
@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController {

    @Autowired
    private ISysDictTypeService dictTypeService;

    @Operation(summary = "查询字典类型列表")
    @GetMapping("/list")
    public Result<Page<SysDictType>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String dictName,
                                          @RequestParam(required = false) String dictType,
                                          @RequestParam(required = false) String status) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictName), SysDictType::getDictName, dictName);
        wrapper.like(StringUtils.hasText(dictType), SysDictType::getDictType, dictType);
        wrapper.eq(StringUtils.hasText(status), SysDictType::getStatus, status);
        wrapper.orderByDesc(SysDictType::getCreateTime);
        return Result.success(dictTypeService.page(page, wrapper));
    }

    @Operation(summary = "获取字典类型详细信息")
    @GetMapping(value = "/{dictId}")
    public Result<SysDictType> getInfo(@PathVariable Long dictId) {
        return Result.success(dictTypeService.getById(dictId));
    }

    @Operation(summary = "新增字典类型")
    @Log("ADD_DICT_TYPE")
    @CacheEvict(cacheNames = "DictDataCache", allEntries = true)
    @PostMapping
    public Result add(@RequestBody SysDictType dict) {
        if (dictTypeService.count(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, dict.getDictType())) > 0) {
            return Result.error("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        return Result.success(dictTypeService.save(dict));
    }

    @Operation(summary = "修改字典类型")
    @Log("UPDATE_DICT_TYPE")
    @CacheEvict(cacheNames = "DictDataCache", allEntries = true)
    @PutMapping
    public Result edit(@RequestBody SysDictType dict) {
        if (dictTypeService.count(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dict.getDictType())
                .ne(SysDictType::getDictId, dict.getDictId())) > 0) {
            return Result.error("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        return Result.success(dictTypeService.updateById(dict));
    }

    @Operation(summary = "删除字典类型")
    @Log("DELETE_DICT_TYPE")
    @CacheEvict(cacheNames = "DictDataCache", allEntries = true)
    @DeleteMapping("/{dictIds}")
    public Result remove(@PathVariable List<Long> dictIds) {
        return Result.success(dictTypeService.removeByIds(dictIds));
    }
    
    @Operation(summary = "获取字典选择框列表")
    @GetMapping("/optionselect")
    public Result<List<SysDictType>> optionselect() {
        return Result.success(dictTypeService.list(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getStatus, "0")));
    }
}
