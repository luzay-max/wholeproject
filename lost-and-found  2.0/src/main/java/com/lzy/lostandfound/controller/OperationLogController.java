package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.entity.OperationLog;
import com.lzy.lostandfound.service.IOperationLogService;
import com.lzy.lostandfound.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/logs")
public class OperationLogController {

    @Autowired
    private IOperationLogService operationLogService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Long userId,
                       @RequestParam(required = false) String actionType,
                       @RequestParam(required = false) String startTime,
                       @RequestParam(required = false) String endTime) {
        
        Page<OperationLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        
        if (StringUtils.hasText(actionType)) {
            wrapper.eq(OperationLog::getActionType, actionType);
        }
        
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        
        if (StringUtils.hasText(endTime)) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }
        
        wrapper.orderByDesc(OperationLog::getCreateTime);
        
        Page<OperationLog> result = operationLogService.page(pageParam, wrapper);
        
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        OperationLog log = operationLogService.getById(id);
        if (log != null) {
            return Result.success(log);
        }
        return Result.error("日志不存在");
    }
}
