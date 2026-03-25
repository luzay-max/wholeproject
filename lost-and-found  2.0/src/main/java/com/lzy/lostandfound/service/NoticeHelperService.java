package com.lzy.lostandfound.service;

import com.lzy.lostandfound.entity.UserNotice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeHelperService {

    private final IUserNoticeService userNoticeService;

    public void sendNotice(String userId, String title, String content, String bizType, String bizId) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        try {
            UserNotice notice = new UserNotice();
            notice.setId(UUID.randomUUID().toString());
            notice.setUserId(userId);
            notice.setTitle(StringUtils.hasText(title) ? title.trim() : "系统通知");
            notice.setContent(StringUtils.hasText(content) ? content.trim() : "您有一条新的通知");
            notice.setBizType(StringUtils.hasText(bizType) ? bizType.trim().toUpperCase() : "SYSTEM");
            notice.setBizId(StringUtils.hasText(bizId) ? bizId.trim() : null);
            notice.setIsRead(0);
            notice.setCreateTime(LocalDateTime.now());
            notice.setUpdateTime(LocalDateTime.now());
            userNoticeService.save(notice);
        } catch (Exception ex) {
            log.warn("发送站内通知失败，userId={}, bizType={}, bizId={}", userId, bizType, bizId, ex);
        }
    }
}

