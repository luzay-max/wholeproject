package com.lzy.lostandfound.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.lostandfound.entity.StudentWhitelist;
import com.lzy.lostandfound.mapper.StudentWhitelistMapper;
import com.lzy.lostandfound.service.IStudentWhitelistService;
import org.springframework.stereotype.Service;

@Service
public class StudentWhitelistServiceImpl extends ServiceImpl<StudentWhitelistMapper, StudentWhitelist> implements IStudentWhitelistService {
}
