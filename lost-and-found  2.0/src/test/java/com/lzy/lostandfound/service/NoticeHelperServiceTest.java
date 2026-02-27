package com.lzy.lostandfound.service;

import com.lzy.lostandfound.entity.UserNotice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NoticeHelperServiceTest {

    @Mock
    private IUserNoticeService userNoticeService;

    private NoticeHelperService noticeHelperService;

    @BeforeEach
    void setUp() {
        noticeHelperService = new NoticeHelperService(userNoticeService);
    }

    @Test
    void sendNoticeShouldSkipWhenUserIdBlank() {
        noticeHelperService.sendNotice(" ", "标题", "内容", "AUDIT", "biz-1");
        verifyNoInteractions(userNoticeService);
    }

    @Test
    void sendNoticeShouldPersistNoticeWithNormalizedBizType() {
        noticeHelperService.sendNotice("user-1", "审核通过", "您的信息已通过审核", "audit", "lost-1");

        ArgumentCaptor<UserNotice> captor = ArgumentCaptor.forClass(UserNotice.class);
        verify(userNoticeService).save(captor.capture());

        UserNotice notice = captor.getValue();
        assertNotNull(notice.getId());
        assertEquals("user-1", notice.getUserId());
        assertEquals("审核通过", notice.getTitle());
        assertEquals("您的信息已通过审核", notice.getContent());
        assertEquals("AUDIT", notice.getBizType());
        assertEquals("lost-1", notice.getBizId());
        assertEquals(0, notice.getIsRead());
    }
}

