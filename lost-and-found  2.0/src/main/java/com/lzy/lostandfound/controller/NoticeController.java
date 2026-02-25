package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.entity.UserNotice;
import com.lzy.lostandfound.service.IUserNoticeService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private IUserNoticeService userNoticeService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer isRead) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims == null ? null : String.valueOf(claims.get("id"));
        if (userId == null) {
            return Result.notLogin("登录状态失效，请重新登录");
        }

        Page<UserNotice> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserNotice> wrapper = new LambdaQueryWrapper<UserNotice>()
                .eq(UserNotice::getUserId, userId)
                .orderByDesc(UserNotice::getCreateTime);
        if (isRead != null) {
            wrapper.eq(UserNotice::getIsRead, isRead);
        }
        userNoticeService.page(pageInfo, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getRecords());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("current", pageInfo.getCurrent());
        data.put("size", pageInfo.getSize());
        return Result.success(data);
    }

    @GetMapping("/unread-count")
    public Result unreadCount() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims == null ? null : String.valueOf(claims.get("id"));
        if (userId == null) {
            return Result.notLogin("登录状态失效，请重新登录");
        }
        long count = userNoticeService.count(new LambdaQueryWrapper<UserNotice>()
                .eq(UserNotice::getUserId, userId)
                .eq(UserNotice::getIsRead, 0));
        return Result.success(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    public Result markRead(@PathVariable String id) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims == null ? null : String.valueOf(claims.get("id"));
        if (userId == null) {
            return Result.notLogin("登录状态失效，请重新登录");
        }

        boolean updated = userNoticeService.update(
                new LambdaUpdateWrapper<UserNotice>()
                        .eq(UserNotice::getId, id)
                        .eq(UserNotice::getUserId, userId)
                        .set(UserNotice::getIsRead, 1)
                        .set(UserNotice::getReadTime, LocalDateTime.now())
                        .set(UserNotice::getUpdateTime, LocalDateTime.now())
        );
        return updated ? Result.success("已标记已读") : Result.error("通知不存在或无权限");
    }

    @PutMapping("/read-all")
    public Result markAllRead() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String userId = claims == null ? null : String.valueOf(claims.get("id"));
        if (userId == null) {
            return Result.notLogin("登录状态失效，请重新登录");
        }
        userNoticeService.update(
                new LambdaUpdateWrapper<UserNotice>()
                        .eq(UserNotice::getUserId, userId)
                        .eq(UserNotice::getIsRead, 0)
                        .set(UserNotice::getIsRead, 1)
                        .set(UserNotice::getReadTime, LocalDateTime.now())
                        .set(UserNotice::getUpdateTime, LocalDateTime.now())
        );
        return Result.success("已全部标记已读");
    }
}

