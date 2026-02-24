package com.lzy.lostandfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzy.lostandfound.entity.SysDictData;

import java.util.List;

public interface ISysDictDataService extends IService<SysDictData> {
    List<SysDictData> selectDictDataByType(String dictType);
}
