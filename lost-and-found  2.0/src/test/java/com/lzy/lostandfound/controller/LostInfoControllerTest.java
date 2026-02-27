package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.dto.LostInfoCreateRequest;
import com.lzy.lostandfound.entity.LostInfo;
import com.lzy.lostandfound.service.IActivitiesService;
import com.lzy.lostandfound.service.ILostInfoService;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.service.RiskControlService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LostInfoControllerTest {

    @InjectMocks
    private LostInfoController lostInfoController;

    @Mock
    private ILostInfoService lostInfoService;
    @Mock
    private IActivitiesService activitiesService;
    @Mock
    private IUserService userService;
    @Mock
    private RiskControlService riskControlService;

    @BeforeEach
    void setUp() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", "user-1");
        ThreadLocalUtil.set(claims);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalUtil.remove();
    }

    @Test
    void addLostInfoShouldRejectWhenLostTimeIsFuture() {
        LostInfoCreateRequest request = buildRequest(String.valueOf(System.currentTimeMillis() + 3600_000L));

        when(riskControlService.checkPublishRate("user-1")).thenReturn(null);
        when(riskControlService.checkSensitiveContent(anyString(), anyString())).thenReturn(null);

        Result result = lostInfoController.addLostInfo(request);

        assertEquals(1, result.getCode());
        assertTrue(result.getMessage().contains("丢失时间不能晚于当前时间"));
        verify(lostInfoService, never()).save(any(LostInfo.class));
    }

    @Test
    void addLostInfoShouldSavePendingRecordWhenValid() {
        LostInfoCreateRequest request = buildRequest(String.valueOf(System.currentTimeMillis() - 3600_000L));

        when(riskControlService.checkPublishRate("user-1")).thenReturn(null);
        when(riskControlService.checkSensitiveContent(anyString(), anyString())).thenReturn(null);

        Result result = lostInfoController.addLostInfo(request);

        assertEquals(0, result.getCode());
        assertEquals("发布成功，等待审核", result.getData());

        ArgumentCaptor<LostInfo> captor = ArgumentCaptor.forClass(LostInfo.class);
        verify(lostInfoService).save(captor.capture());
        LostInfo saved = captor.getValue();
        assertEquals("user-1", saved.getUserId());
        assertEquals("PENDING", saved.getStatus());
        assertEquals("校园卡", saved.getName());
    }

    private LostInfoCreateRequest buildRequest(String lostTime) {
        LostInfoCreateRequest request = new LostInfoCreateRequest();
        request.setName("校园卡");
        request.setType("ID_CARD");
        request.setLocation("图书馆三楼");
        request.setDescription("蓝色校园卡，卡套上有挂绳");
        request.setImages("[]");
        request.setContactName("张三");
        request.setContactPhone("13800000001");
        request.setContactEmail("user1@test.com");
        request.setLostTime(lostTime);
        return request;
    }
}

