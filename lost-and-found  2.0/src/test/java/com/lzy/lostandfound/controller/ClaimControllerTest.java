package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.dto.ClaimApplyRequest;
import com.lzy.lostandfound.dto.ClaimConfirmRequest;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimControllerTest {

    @InjectMocks
    private ClaimController claimController;

    @Mock
    private IClaimOrderService claimOrderService;
    @Mock
    private ILostInfoService lostInfoService;
    @Mock
    private IFindInfoService findInfoService;
    @Mock
    private NoticeHelperService noticeHelperService;
    @Mock
    private IActivitiesService activitiesService;

    @AfterEach
    void tearDown() {
        ThreadLocalUtil.remove();
    }

    @Test
    void applyShouldCreateOrderForFindInfo() {
        setClaims("applicant-1");
        ClaimApplyRequest request = new ClaimApplyRequest();
        request.setItemType("find");
        request.setItemId("find-1");
        request.setApplyNote("这是我的物品");

        FindInfo findInfo = new FindInfo();
        findInfo.setId("find-1");
        findInfo.setName("黑色钱包");
        findInfo.setUserId("publisher-1");
        findInfo.setStatus("APPROVED");
        when(findInfoService.getById("find-1")).thenReturn(findInfo);
        when(claimOrderService.count(any())).thenReturn(0L);

        Result result = claimController.apply(request);

        assertEquals(0, result.getCode());
        ArgumentCaptor<ClaimOrder> captor = ArgumentCaptor.forClass(ClaimOrder.class);
        verify(claimOrderService).save(captor.capture());
        ClaimOrder saved = captor.getValue();
        assertEquals("APPLIED", saved.getStatus());
        assertEquals("publisher-1", saved.getPublisherUserId());
        verify(noticeHelperService).sendNotice(
                eq("publisher-1"),
                eq("您有新的认领申请"),
                contains("黑色钱包"),
                eq("CLAIM"),
                eq(saved.getId())
        );
        verify(activitiesService).save(any(Activities.class));
    }

    @Test
    void applyShouldRejectLostType() {
        setClaims("applicant-1");
        ClaimApplyRequest request = new ClaimApplyRequest();
        request.setItemType("lost");
        request.setItemId("lost-1");

        Result result = claimController.apply(request);

        assertEquals(1, result.getCode());
        assertTrue(result.getMessage().contains("我找到了"));
        verify(claimOrderService, never()).save(any());
    }

    @Test
    void confirmShouldRejectWhenReasonMissing() {
        setClaims("publisher-1");
        ClaimOrder order = new ClaimOrder();
        order.setId("claim-1");
        order.setItemName("校园卡");
        order.setItemType("find");
        order.setItemId("find-1");
        order.setPublisherUserId("publisher-1");
        order.setApplicantUserId("applicant-1");
        order.setStatus("PROOF_SUBMITTED");
        when(claimOrderService.getById("claim-1")).thenReturn(order);

        ClaimConfirmRequest request = new ClaimConfirmRequest();
        request.setApproved(false);
        request.setRejectReason(" ");

        Result result = claimController.confirm("claim-1", request);

        assertEquals(1, result.getCode());
        assertTrue(result.getMessage().contains("请填写驳回原因"));
        verify(claimOrderService, never()).updateById(any(ClaimOrder.class));
    }

    @Test
    void completeShouldMarkLostInfoSolved() {
        setClaims("applicant-1");
        ClaimOrder order = new ClaimOrder();
        order.setId("claim-1");
        order.setItemType("lost");
        order.setItemId("lost-1");
        order.setItemName("校园卡");
        order.setPublisherUserId("publisher-1");
        order.setApplicantUserId("applicant-1");
        order.setStatus("CONFIRMED");
        when(claimOrderService.getById("claim-1")).thenReturn(order);

        LostInfo lostInfo = new LostInfo();
        lostInfo.setId("lost-1");
        lostInfo.setStatus("APPROVED");
        when(lostInfoService.getById("lost-1")).thenReturn(lostInfo);

        Result result = claimController.complete("claim-1");

        assertEquals(0, result.getCode());
        verify(claimOrderService).updateById(any(ClaimOrder.class));
        verify(lostInfoService).updateById(any(LostInfo.class));
        verify(noticeHelperService, times(2)).sendNotice(any(), any(), any(), eq("CLAIM"), eq("claim-1"));
        verify(activitiesService).save(any(Activities.class));
    }

    private void setClaims(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        ThreadLocalUtil.set(claims);
    }
}

