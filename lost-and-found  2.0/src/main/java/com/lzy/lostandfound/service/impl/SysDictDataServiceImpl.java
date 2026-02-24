package com.lzy.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.lostandfound.entity.SysDictData;
import com.lzy.lostandfound.mapper.SysDictDataMapper;
import com.lzy.lostandfound.service.ISysDictDataService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return baseMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getStatus, "0")
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort));
    }
}
