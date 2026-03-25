package com.lzy.lostandfound.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.lostandfound.entity.UserNotice;
import com.lzy.lostandfound.mapper.UserNoticeMapper;
import com.lzy.lostandfound.service.IUserNoticeService;
import org.springframework.stereotype.Service;

@Service
public class UserNoticeServiceImpl extends ServiceImpl<UserNoticeMapper, UserNotice> implements IUserNoticeService {
}

