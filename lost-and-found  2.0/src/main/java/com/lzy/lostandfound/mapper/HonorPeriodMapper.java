package com.lzy.lostandfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzy.lostandfound.entity.HonorPeriod;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface HonorPeriodMapper extends BaseMapper<HonorPeriod> {

    @Update("UPDATE honor_period SET is_deleted = 0 WHERE id = #{id} AND is_deleted = 1")
    int restoreById(@Param("id") String id);
}

