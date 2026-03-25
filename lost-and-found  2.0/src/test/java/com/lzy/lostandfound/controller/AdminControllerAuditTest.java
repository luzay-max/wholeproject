package com.lzy.lostandfound.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzy.lostandfound.entity.Activities;
import com.lzy.lostandfound.entity.Comment;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.entity.LostInfo;
import com.lzy.lostandfound.service.IActivitiesService;
import com.lzy.lostandfound.service.ICommentService;
import com.lzy.lostandfound.service.IFindInfoService;
import com.lzy.lostandfound.service.ILostInfoService;
import com.lzy.lostandfound.service.NoticeHelperService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.IdAndType;
import com.lzy.lostandfound.vo.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerAuditTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private ILostInfoService lostInfoService;
    @Mock
    private IFindInfoService findInfoService;
    @Mock
    private IActivitiesService activitiesService;
    @Mock
    private ICommentService commentService;
    @Mock
    private NoticeHelperService noticeHelperService;

    @BeforeEach
    void setUp() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", "admin-1");
        claims.put("role", "ADMIN");
        ThreadLocalUtil.set(claims);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalUtil.remove();
    }

    @Test
    void passShouldApproveLostInfoAndSendNotice() {
        LostInfo lostInfo = new LostInfo();
        lostInfo.setId("lost-1");
        lostInfo.setUserId("publisher-1");
        lostInfo.setName("校园卡");
        when(lostInfoService.getById("lost-1")).thenReturn(lostInfo);

        Result result = adminController.pass(new IdAndType("lost-1", "lost", null));

        assertEquals(0, result.getCode());
        verify(lostInfoService).updateById(any(LostInfo.class));
        verify(activitiesService).save(any(Activities.class));
        verify(noticeHelperService).sendNotice(
                eq("publisher-1"),
                eq("审核通过"),
                contains("校园卡"),
                eq("AUDIT"),
                eq("lost-1")
        );
    }

    @Test
    void rejectShouldWriteManagerCommentAndNotifyPublisher() {
        FindInfo findInfo = new FindInfo();
        findInfo.setId("find-1");
        findInfo.setUserId("publisher-2");
        findInfo.setName("黑色雨伞");
        when(findInfoService.getById("find-1")).thenReturn(findInfo);

        Result result = adminController.reject(new IdAndType("find-1", "find", "违规内容"));

        assertEquals(0, result.getCode());
        verify(findInfoService).updateById(any(FindInfo.class));
        verify(commentService).save(any(Comment.class));
        verify(noticeHelperService).sendNotice(
                eq("publisher-2"),
                eq("审核未通过"),
                contains("违规内容"),
                eq("AUDIT"),
                eq("find-1")
        );
    }

    @Test
    void batchPassShouldAcceptObjectWithIds() throws Exception {
        LostInfo lostInfo = new LostInfo();
        lostInfo.setId("lost-2");
        lostInfo.setUserId("publisher-3");
        lostInfo.setName("蓝色水杯");
        when(lostInfoService.getById("lost-2")).thenReturn(lostInfo);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode payload = mapper.readTree("{\"ids\":[\"lost-2\"]}");
        Result result = adminController.batchPass(payload);

        assertEquals(0, result.getCode());
        verify(lostInfoService).updateById(any(LostInfo.class));
        verify(noticeHelperService).sendNotice(
                eq("publisher-3"),
                eq("审核通过"),
                contains("蓝色水杯"),
                eq("AUDIT"),
                eq("lost-2")
        );
    }
}
