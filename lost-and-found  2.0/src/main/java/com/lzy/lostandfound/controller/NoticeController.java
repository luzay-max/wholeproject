package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.entity.Activities;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.entity.LostInfo;
import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.entity.UserNotice;
import com.lzy.lostandfound.service.IActivitiesService;
import com.lzy.lostandfound.service.IFindInfoService;
import com.lzy.lostandfound.service.ILostInfoService;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.service.IUserNoticeService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    private static final Pattern REPORTER_ID_PATTERN = Pattern.compile("举报人ID[:：]\\s*([0-9A-Za-z\\-]+)");
    private static final Pattern REPORTER_NAME_PATTERN = Pattern.compile("^\\s*([^\\s]+)\\s*举报了");

    @Autowired
    private IUserNoticeService userNoticeService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ILostInfoService lostInfoService;
    @Autowired
    private IFindInfoService findInfoService;
    @Autowired
    private IActivitiesService activitiesService;

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
        pageInfo.getRecords().forEach(this::enrichReportNotice);

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

    @PutMapping("/report/{id}/ban-reporter")
    public Result banReportReporter(@PathVariable String id) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        String operatorId = claims == null ? null : String.valueOf(claims.get("id"));
        if (!StringUtils.hasText(operatorId)) {
            return Result.notLogin("登录状态失效，请重新登录");
        }

        User operator = userService.getById(operatorId);
        if (operator == null || !"ADMIN".equalsIgnoreCase(operator.getRole())) {
            return Result.forbidden("仅管理员可执行此操作");
        }

        UserNotice notice = userNoticeService.getById(id);
        if (notice == null) {
            return Result.error("通知不存在");
        }
        if (!operatorId.equals(notice.getUserId())) {
            return Result.forbidden("无权限操作该通知");
        }
        if (!"REPORT".equalsIgnoreCase(notice.getBizType())) {
            return Result.error("该通知不是举报通知");
        }

        ReportMeta meta = parseReportMeta(notice);
        String reporterId = resolveReporterId(notice, meta);
        if (!StringUtils.hasText(reporterId)) {
            return Result.error("未识别到举报人ID");
        }

        User target = userService.getById(reporterId);
        if (target == null) {
            return Result.error("举报人不存在或已删除");
        }
        if (operatorId.equals(target.getId())) {
            return Result.error("禁止封禁当前登录账号");
        }
        if ("ADMIN".equalsIgnoreCase(target.getRole())) {
            return Result.error("管理员不可被封禁");
        }
        if (target.getStatus() != null && target.getStatus() == 1) {
            return Result.success("该用户已是封禁状态");
        }

        target.setStatus(1);
        target.setUpdateTime(LocalDateTime.now());
        boolean updated = userService.updateById(target);
        if (!updated) {
            return Result.error("封禁失败，请稍后重试");
        }

        // 识别成功后回写通知，避免后续重复识别失败
        String itemType = normalizeItemType(meta.itemType());
        String itemId = meta.itemId();
        if (StringUtils.hasText(itemId)) {
            notice.setBizId(itemType + ":" + itemId + ":" + reporterId);
        }
        if (!StringUtils.hasText(extractReporterIdFromContent(notice.getContent()))) {
            String content = StringUtils.hasText(notice.getContent()) ? notice.getContent() : "收到新的举报";
            notice.setContent(content + "，举报人ID：" + reporterId);
        }
        notice.setUpdateTime(LocalDateTime.now());
        userNoticeService.updateById(notice);

        return Result.success("已封禁举报人：" + target.getId());
    }

    private ReportMeta parseReportMeta(UserNotice notice) {
        String bizId = notice.getBizId() == null ? "" : notice.getBizId().trim();
        String content = notice.getContent();
        if (bizId.contains(":")) {
            String[] parts = bizId.split(":");
            if (parts.length >= 3) {
                return new ReportMeta(normalizeItemType(parts[0]), parts[1], parts[2]);
            }
            if (parts.length == 2) {
                return new ReportMeta(normalizeItemType(parts[0]), parts[1], extractReporterIdFromContent(content));
            }
        }
        String itemType = inferItemType(bizId, content);
        return new ReportMeta(itemType, bizId, extractReporterIdFromContent(content));
    }

    private String normalizeItemType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "lost";
        }
        String val = raw.trim().toLowerCase();
        return "find".equals(val) ? "find" : "lost";
    }

    private String extractReporterIdFromContent(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        Matcher matcher = REPORTER_ID_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractReporterNameFromContent(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        Matcher matcher = REPORTER_NAME_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String inferItemType(String itemId, String content) {
        String text = content == null ? "" : content;
        if (text.contains("招领")) {
            return "find";
        }
        if (text.contains("失物")) {
            return "lost";
        }
        if (StringUtils.hasText(itemId)) {
            LostInfo lostInfo = lostInfoService.getById(itemId);
            if (lostInfo != null) {
                return "lost";
            }
            FindInfo findInfo = findInfoService.getById(itemId);
            if (findInfo != null) {
                return "find";
            }
        }
        return "lost";
    }

    private String resolveReporterId(UserNotice notice, ReportMeta meta) {
        if (StringUtils.hasText(meta.reporterId())) {
            return meta.reporterId();
        }

        String byContent = extractReporterIdFromContent(notice.getContent());
        if (StringUtils.hasText(byContent)) {
            return byContent;
        }

        String reporterName = extractReporterNameFromContent(notice.getContent());
        if (StringUtils.hasText(reporterName)) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<User>()
                    .and(w -> w.eq(User::getAccountName, reporterName).or().eq(User::getUsername, reporterName))
                    .last("limit 1");
            User user = userService.getOne(userWrapper, false);
            if (user != null && StringUtils.hasText(user.getId())) {
                return user.getId();
            }
        }

        if (StringUtils.hasText(meta.itemId())) {
            LambdaQueryWrapper<Activities> actWrapper = new LambdaQueryWrapper<Activities>()
                    .eq(Activities::getAction, "REPORT")
                    .eq(Activities::getItemId, meta.itemId())
                    .orderByDesc(Activities::getCreateTime)
                    .last("limit 20");
            if ("lost".equals(meta.itemType()) || "find".equals(meta.itemType())) {
                actWrapper.eq(Activities::getItemType, meta.itemType());
            }
            List<Activities> acts = activitiesService.list(actWrapper);
            if (!acts.isEmpty()) {
                Activities nearest = acts.stream()
                        .min(Comparator.comparingLong(a -> {
                            LocalDateTime noticeTime = notice.getCreateTime();
                            LocalDateTime actTime = a.getCreateTime();
                            if (noticeTime == null || actTime == null) {
                                return Long.MAX_VALUE / 4;
                            }
                            return Math.abs(java.time.Duration.between(noticeTime, actTime).toMillis());
                        }))
                        .orElse(acts.get(0));
                if (nearest != null && StringUtils.hasText(nearest.getUserId())) {
                    return nearest.getUserId();
                }
            }
        }

        return "";
    }

    private void enrichReportNotice(UserNotice notice) {
        if (notice == null || !"REPORT".equalsIgnoreCase(notice.getBizType())) {
            return;
        }
        ReportMeta meta = parseReportMeta(notice);
        String itemType = normalizeItemType(meta.itemType());
        String itemId = meta.itemId();
        String reporterId = resolveReporterId(notice, meta);

        if (StringUtils.hasText(itemId)) {
            notice.setBizId(itemType + ":" + itemId + ":" + (StringUtils.hasText(reporterId) ? reporterId : ""));
        }
        if (StringUtils.hasText(reporterId) && !StringUtils.hasText(extractReporterIdFromContent(notice.getContent()))) {
            String content = StringUtils.hasText(notice.getContent()) ? notice.getContent() : "收到新的举报";
            notice.setContent(content + "，举报人ID：" + reporterId);
        }
    }

    private record ReportMeta(String itemType, String itemId, String reporterId) {
    }
}
