package com.lzy.lostandfound.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.lostandfound.entity.HonorPeriodItem;
import com.lzy.lostandfound.mapper.HonorPeriodItemMapper;
import com.lzy.lostandfound.service.IHonorPeriodItemService;
import org.springframework.stereotype.Service;

@Service
public class HonorPeriodItemServiceImpl extends ServiceImpl<HonorPeriodItemMapper, HonorPeriodItem> implements IHonorPeriodItemService {
}

