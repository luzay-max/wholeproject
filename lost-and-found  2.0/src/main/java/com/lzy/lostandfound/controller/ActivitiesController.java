package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzy.lostandfound.entity.Activities;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.service.IActivitiesService;
import com.lzy.lostandfound.service.IFindInfoService;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 用户活动日志表 前端控制器
 * </p>
 *
 * 示例接口：
 * http://localhost:8080/api/activity/recent?limit=10&type=all
 *
 * 请求方法：
 * GET
 *
 * 前端期望返回示例：
 * [
 *   { userName: '张三', action: '发布了失物信息', itemName: '校园卡', type: 'lost', time: 1730798893000 }
 * ]
 */
@RestController
@RequestMapping("/activity")
public class ActivitiesController {

    @Autowired
    private IActivitiesService activitiesService;
    @Autowired
    private IUserService userService;

    /**
     * 查询最近活动记录
     */
    @GetMapping("/recent")
    public Result recent(@RequestParam(value = "limit", defaultValue = "8") Integer limit,
                         @RequestParam(value = "type", defaultValue = "all") String type) {
        try {
            LambdaQueryWrapper<Activities> queryWrapper = new LambdaQueryWrapper<>();

            // 类型筛选
            if (!"all".equals(type)) {
                queryWrapper.eq(Activities::getItemType, type);
            }

            // 时间倒序 & 限制数量
            queryWrapper.orderByDesc(Activities::getCreateTime);
            queryWrapper.last("LIMIT " + limit);

            List<Activities> list = activitiesService.list(queryWrapper);

            // 构建前端需要的返回格式
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Activities act : list) {
                Map<String, Object> map = new HashMap<>();
                User user = userService.getById(act.getUserId());
                String username = user.getUsername();
                map.put("userName", username); // 这里建议后期关联 user 表获取用户名
                map.put("action", act.getAction());
                map.put("itemName", act.getContent());
                map.put("type", act.getItemType());
                map.put("time", act.getCreateTime());
                resultList.add(map);
            }

            return Result.success(resultList);
        } catch (Exception e) {
            com.lzy.lostandfound.utils.LogUtil.error("执行异常", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 在更新（或发布）操作中插入活动日志
     * 例如在 FindInfo 更新后调用该接口
     */
    @PostMapping("/log")
    public Result logActivity(@RequestBody Activities activity) {
        try {
            activity.setId(UUID.randomUUID().toString());
            activity.setCreateTime(LocalDateTime.now());

            boolean saved = activitiesService.save(activity);
            if (saved) {
                return Result.success("活动记录已添加");
            } else {
                return Result.error("插入活动记录失败");
            }
        } catch (Exception e) {
            com.lzy.lostandfound.utils.LogUtil.error("执行异常", e);
            return Result.error("插入异常：" + e.getMessage());
        }
    }
}

