package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.dto.ClaimApplyRequest;
import com.lzy.lostandfound.dto.ClaimConfirmRequest;
import com.lzy.lostandfound.dto.ClaimFoundReportRequest;
import com.lzy.lostandfound.dto.ClaimProofRequest;
import com.lzy.lostandfound.entity.Activities;
import com.lzy.lostandfound.entity.ClaimOrder;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.entity.LostInfo;
import com.lzy.lostandfound.service.IActivitiesService;
import com.lzy.lostandfound.service.IClaimOrderService;
import com.lzy.lostandfound.service.IFindInfoService;
import com.lzy.lostandfound.service.ILostInfoService;
import com.lzy.lostandfound.service.NoticeHelperService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/claim")
public class ClaimController {

    @Autowired
    private IClaimOrderService claimOrderService;
    @Autowired
    private ILostInfoService lostInfoService;
    @Autowired
    private IFindInfoService findInfoService;
    @Autowired
    private NoticeHelperService noticeHelperService;
    @Autowired
    private IActivitiesService activitiesService;

    @PostMapping("/apply")
    @Log("CLAIM_APPLY")
    public Result apply(@Valid @RequestBody ClaimApplyRequest request) {
        String currentUserId = currentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return Result.notLogin("登录状态失效，请重新登录");
        }

        String itemType = request.getItemType().trim().toLowerCase();
        if (!"find".equals(itemType)) {
            return Result.error("失物信息请使用“我找到了”功能提醒发布者");
        }
        ItemSnapshot snapshot = loadSnapshot(itemType, request.getItemId());
        if (snapshot == null) {
            return Result.error("信息不存在");
        }
        if (!"APPROVED".equalsIgnoreCase(snapshot.status)) {
            return Result.error("仅可认领已发布的信息");
        }
        if (currentUserId.equals(snapshot.publisherUserId)) {
            return Result.error("不能认领自己发布的信息");
        }

        long activeCount = claimOrderService.count(new LambdaQueryWrapper<ClaimOrder>()
                .eq(ClaimOrder::getItemType, itemType)
                .eq(ClaimOrder::getItemId, request.getItemId())
                .eq(ClaimOrder::getApplicantUserId, currentUserId)
                .in(ClaimOrder::getStatus, "APPLIED", "PROOF_SUBMITTED", "CONFIRMED"));
        if (activeCount > 0) {
            return Result.error("您已提交过认领申请，请勿重复提交");
        }

        ClaimOrder order = new ClaimOrder();
        order.setId(UUID.randomUUID().toString());
        order.setItemType(itemType);
        order.setItemId(request.getItemId());
        order.setItemName(snapshot.itemName);
        order.setPublisherUserId(snapshot.publisherUserId);
        order.setApplicantUserId(currentUserId);
        order.setApplyNote(StringUtils.hasText(request.getApplyNote()) ? request.getApplyNote().trim() : null);
        order.setStatus("APPLIED");
        order.setApplyTime(LocalDateTime.now());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        claimOrderService.save(order);

        noticeHelperService.sendNotice(
                snapshot.publisherUserId,
                "您有新的认领申请",
                "有人申请认领您发布的「" + snapshot.itemName + "」，请及时处理。",
                "CLAIM",
                order.getId()
        );
        addActivity(currentUserId, "CLAIM_APPLY", itemType, request.getItemId(), "提交认领申请：" + snapshot.itemName);
        return Result.success(order);
    }

    @PostMapping("/report-found")
    @Log("REPORT_FOUND")
    public Result reportFound(@Valid @RequestBody ClaimFoundReportRequest request) {
        String currentUserId = currentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return Result.notLogin("登录状态失效，请重新登录");
        }

        LostInfo lostInfo = lostInfoService.getById(request.getItemId());
        if (lostInfo == null) {
            return Result.error("失物信息不存在");
        }
        if (!"APPROVED".equalsIgnoreCase(lostInfo.getStatus())) {
            return Result.error("仅可对已发布的失物进行提醒");
        }
        if (currentUserId.equals(lostInfo.getUserId())) {
            return Result.error("不能提醒自己发布的失物信息");
        }

        String note = StringUtils.hasText(request.getNote()) ? request.getNote().trim() : "有人反馈可能找到了该失物，请尽快联系确认。";
        noticeHelperService.sendNotice(
                lostInfo.getUserId(),
                "有人反馈找到了您的失物",
                "失物「" + lostInfo.getName() + "」收到新线索：" + note,
                "FOUND_REPORT",
                lostInfo.getId()
        );
        addActivity(currentUserId, "REPORT_FOUND", "lost", lostInfo.getId(), "反馈找到失物线索：" + lostInfo.getName());
        return Result.success("已提醒发布者，请等待对方联系");
    }

    @PostMapping("/{id}/proof")
    @Log("CLAIM_SUBMIT_PROOF")
    public Result submitProof(@PathVariable String id, @Valid @RequestBody ClaimProofRequest request) {
        String currentUserId = currentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return Result.notLogin("登录状态失效，请重新登录");
        }
        ClaimOrder order = claimOrderService.getById(id);
        if (order == null) {
            return Result.error("认领申请不存在");
        }
        if (!currentUserId.equals(order.getApplicantUserId())) {
            return Result.forbidden("仅申请人可提交凭证");
        }
        if (!"APPLIED".equalsIgnoreCase(order.getStatus())) {
            return Result.error("当前状态不允许提交凭证");
        }
        order.setProofText(request.getProofText().trim());
        order.setProofImages(StringUtils.hasText(request.getProofImages()) ? request.getProofImages().trim() : null);
        order.setStatus("PROOF_SUBMITTED");
        order.setProofSubmitTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        claimOrderService.updateById(order);

        noticeHelperService.sendNotice(
                order.getPublisherUserId(),
                "认领凭证已提交",
                "认领申请「" + order.getItemName() + "」已提交凭证，请审核确认。",
                "CLAIM",
                order.getId()
        );
        addActivity(currentUserId, "CLAIM_PROOF", order.getItemType(), order.getItemId(), "提交认领凭证：" + order.getItemName());
        return Result.success("凭证提交成功");
    }

    @PostMapping("/{id}/confirm")
    @Log("CLAIM_CONFIRM")
    public Result confirm(@PathVariable String id, @Valid @RequestBody ClaimConfirmRequest request) {
        String currentUserId = currentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return Result.notLogin("登录状态失效，请重新登录");
        }
        ClaimOrder order = claimOrderService.getById(id);
        if (order == null) {
            return Result.error("认领申请不存在");
        }
        if (!currentUserId.equals(order.getPublisherUserId())) {
            return Result.forbidden("仅发布者可确认认领");
        }
        if (!"PROOF_SUBMITTED".equalsIgnoreCase(order.getStatus())) {
            return Result.error("当前状态不允许确认");
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            order.setStatus("CONFIRMED");
            order.setConfirmTime(LocalDateTime.now());
            order.setRejectReason(null);
            noticeHelperService.sendNotice(
                    order.getApplicantUserId(),
                    "认领申请已通过",
                    "您对「" + order.getItemName() + "」的认领申请已通过，请完成归还并点击完成。",
                    "CLAIM",
                    order.getId()
            );
            addActivity(currentUserId, "CLAIM_CONFIRM", order.getItemType(), order.getItemId(), "确认认领通过：" + order.getItemName());
        } else {
            if (!StringUtils.hasText(request.getRejectReason())) {
                return Result.error("请填写驳回原因");
            }
            order.setStatus("REJECTED");
            order.setRejectReason(request.getRejectReason().trim());
            noticeHelperService.sendNotice(
                    order.getApplicantUserId(),
                    "认领申请被驳回",
                    "您对「" + order.getItemName() + "」的认领申请被驳回，原因：" + order.getRejectReason(),
                    "CLAIM",
                    order.getId()
            );
            addActivity(currentUserId, "CLAIM_REJECT", order.getItemType(), order.getItemId(), "驳回认领申请：" + order.getItemName());
        }

        order.setUpdateTime(LocalDateTime.now());
        claimOrderService.updateById(order);
        return Result.success(Boolean.TRUE.equals(request.getApproved()) ? "确认成功" : "已驳回");
    }

    @PostMapping("/{id}/complete")
    @Log("CLAIM_COMPLETE")
    public Result complete(@PathVariable String id) {
        String currentUserId = currentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return Result.notLogin("登录状态失效，请重新登录");
        }
        ClaimOrder order = claimOrderService.getById(id);
        if (order == null) {
            return Result.error("认领申请不存在");
        }
        if (!currentUserId.equals(order.getApplicantUserId()) && !currentUserId.equals(order.getPublisherUserId())) {
            return Result.forbidden("仅申请人或发布者可完成归还");
        }
        if (!"CONFIRMED".equalsIgnoreCase(order.getStatus())) {
            return Result.error("当前状态不允许完成归还");
        }

        order.setStatus("COMPLETED");
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        claimOrderService.updateById(order);
        markItemSolved(order.getItemType(), order.getItemId());

        noticeHelperService.sendNotice(
                order.getApplicantUserId(),
                "认领流程已完成",
                "「" + order.getItemName() + "」认领流程已完成，感谢您的参与。",
                "CLAIM",
                order.getId()
        );
        noticeHelperService.sendNotice(
                order.getPublisherUserId(),
                "归还流程已完成",
                "「" + order.getItemName() + "」归还流程已完成。",
                "CLAIM",
                order.getId()
        );
        addActivity(currentUserId, "CLAIM_COMPLETE", order.getItemType(), order.getItemId(), "完成归还：" + order.getItemName());
        return Result.success("已完成归还");
    }

    @GetMapping("/my/applications")
    public Result myApplications(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String status) {
        String currentUserId = currentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return Result.notLogin("登录状态失效，请重新登录");
        }
        Page<ClaimOrder> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<ClaimOrder> wrapper = new LambdaQueryWrapper<ClaimOrder>()
                .eq(ClaimOrder::getApplicantUserId, currentUserId)
                .orderByDesc(ClaimOrder::getCreateTime);
        if (StringUtils.hasText(status)) {
            wrapper.eq(ClaimOrder::getStatus, status.trim().toUpperCase());
        }
        claimOrderService.page(pageInfo, wrapper);
        return Result.success(buildPageData(pageInfo));
    }

    @GetMapping("/my/pending-confirm")
    public Result myPendingConfirm(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String status) {
        String currentUserId = currentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return Result.notLogin("登录状态失效，请重新登录");
        }
        Page<ClaimOrder> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<ClaimOrder> wrapper = new LambdaQueryWrapper<ClaimOrder>()
                .eq(ClaimOrder::getPublisherUserId, currentUserId)
                .orderByDesc(ClaimOrder::getCreateTime);
        if (StringUtils.hasText(status)) {
            wrapper.eq(ClaimOrder::getStatus, status.trim().toUpperCase());
        }
        claimOrderService.page(pageInfo, wrapper);
        return Result.success(buildPageData(pageInfo));
    }

    private Map<String, Object> buildPageData(Page<ClaimOrder> pageInfo) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getRecords());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("current", pageInfo.getCurrent());
        data.put("size", pageInfo.getSize());
        return data;
    }

    private void markItemSolved(String itemType, String itemId) {
        if ("lost".equalsIgnoreCase(itemType)) {
            LostInfo lostInfo = lostInfoService.getById(itemId);
            if (lostInfo != null) {
                lostInfo.setStatus("SOLVED");
                lostInfo.setUpdateTime(LocalDateTime.now());
                lostInfoService.updateById(lostInfo);
            }
            return;
        }
        FindInfo findInfo = findInfoService.getById(itemId);
        if (findInfo != null) {
            findInfo.setStatus("SOLVED");
            findInfo.setUpdateTime(LocalDateTime.now());
            findInfoService.updateById(findInfo);
        }
    }

    private void addActivity(String userId, String action, String itemType, String itemId, String content) {
        Activities act = new Activities();
        act.setId(UUID.randomUUID().toString());
        act.setUserId(userId);
        act.setAction(action);
        act.setItemType(itemType);
        act.setItemId(itemId);
        act.setContent(content);
        act.setCreateTime(LocalDateTime.now());
        activitiesService.save(act);
    }

    private ItemSnapshot loadSnapshot(String itemType, String itemId) {
        if ("lost".equalsIgnoreCase(itemType)) {
            LostInfo info = lostInfoService.getById(itemId);
            if (info == null) {
                return null;
            }
            return new ItemSnapshot(info.getName(), info.getUserId(), info.getStatus());
        }
        if ("find".equalsIgnoreCase(itemType)) {
            FindInfo info = findInfoService.getById(itemId);
            if (info == null) {
                return null;
            }
            return new ItemSnapshot(info.getName(), info.getUserId(), info.getStatus());
        }
        return null;
    }

    private String currentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        return claims != null && claims.get("id") != null ? String.valueOf(claims.get("id")) : null;
    }

    private record ItemSnapshot(String itemName, String publisherUserId, String status) {
    }
}
