package com.lzy.lostandfound.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.lostandfound.entity.ClaimOrder;
import com.lzy.lostandfound.mapper.ClaimOrderMapper;
import com.lzy.lostandfound.service.IClaimOrderService;
import org.springframework.stereotype.Service;

@Service
public class ClaimOrderServiceImpl extends ServiceImpl<ClaimOrderMapper, ClaimOrder> implements IClaimOrderService {
}

