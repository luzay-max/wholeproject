package com.lzy.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzy.lostandfound.dto.ItemReportRequest;
import com.lzy.lostandfound.entity.Activities;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.entity.LostInfo;
import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.service.IActivitiesService;
import com.lzy.lostandfound.service.IFindInfoService;
import com.lzy.lostandfound.service.ILostInfoService;
import com.lzy.lostandfound.service.IReportService;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.service.NoticeHelperService;
import com.lzy.lostandfound.service.RiskControlService;
import com.lzy.lostandfound.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final ILostInfoService lostInfoService;
    private final IFindInfoService findInfoService;
    private final IActivitiesService activitiesService;
    private final IUserService userService;
    private final NoticeHelperService noticeHelperService;
    private final RiskControlService riskControlService;

    @Override
    public Result reportItem(String reporterId, String reporterName, ItemReportRequest request) {
        try {
            String rateError = riskControlService.checkReportRate(reporterId);
            if (rateError != null) {
                return Result.error(rateError);
            }

            String reason = request.getReason() == null ? "" : request.getReason().trim();
            String sensitiveError = riskControlService.checkSensitiveContent(reason, "举报说明");
            if (sensitiveError != null) {
                return Result.error(sensitiveError);
            }

            String normalizedType = request.getItemType().trim().toLowerCase();
            ItemSnapshot target = loadTarget(normalizedType, request.getItemId());
            if (target == null) {
                return Result.error("举报目标不存在");
            }
            if (reporterId.equals(target.ownerUserId())) {
                return Result.error("不能举报自己发布的信息");
            }

            Activities activity = new Activities();
            activity.setId(UUID.randomUUID().toString());
            activity.setUserId(reporterId);
            activity.setAction("REPORT");
            activity.setItemType(normalizedType);
            activity.setItemId(request.getItemId());
            activity.setContent(buildReportContent(target.itemName(), reason));
            activity.setCreateTime(LocalDateTime.now());
            activity.setIsDeleted(0);
            activitiesService.save(activity);

            notifyAdmins(reporterId, reporterName, normalizedType, request.getItemId(), target.itemName(), reason);
            return Result.success("举报已提交，我们会尽快处理");
        } catch (Exception ex) {
            log.error("举报失败", ex);
            return Result.error("举报失败，请稍后重试");
        }
    }

    private ItemSnapshot loadTarget(String itemType, String itemId) {
        if ("lost".equals(itemType)) {
            LostInfo info = lostInfoService.getById(itemId);
            if (info == null) {
                return null;
            }
            return new ItemSnapshot(info.getName(), info.getUserId());
        }
        FindInfo info = findInfoService.getById(itemId);
        if (info == null) {
            return null;
        }
        return new ItemSnapshot(info.getName(), info.getUserId());
    }

    private String buildReportContent(String itemName, String reason) {
        String title = StringUtils.hasText(itemName) ? itemName : "该信息";
        if (!StringUtils.hasText(reason)) {
            return "举报了信息「" + title + "」";
        }
        return "举报了信息「" + title + "」：" + reason;
    }

    private void notifyAdmins(String reporterId, String reporterName, String itemType, String itemId, String itemName, String reason) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "ADMIN")
                .eq(User::getStatus, 0);
        List<User> admins = userService.list(wrapper);
        String safeReporterName = StringUtils.hasText(reporterName) ? reporterName : "用户";
        String safeName = StringUtils.hasText(itemName) ? itemName : "未命名信息";
        String safeReason = StringUtils.hasText(reason) ? ("，说明：" + reason) : "";
        String safeReporterId = StringUtils.hasText(reporterId) ? reporterId : "unknown";
        String content = safeReporterName + " 举报了" + ("lost".equals(itemType) ? "失物" : "招领") + "「" + safeName + "」"
                + "，举报人ID：" + safeReporterId + safeReason;
        String bizId = itemType + ":" + itemId + ":" + safeReporterId;
        for (User admin : admins) {
            noticeHelperService.sendNotice(
                    admin.getId(),
                    "收到新的举报",
                    content,
                    "REPORT",
                    bizId
            );
        }
    }

    private record ItemSnapshot(String itemName, String ownerUserId) {
    }
}
