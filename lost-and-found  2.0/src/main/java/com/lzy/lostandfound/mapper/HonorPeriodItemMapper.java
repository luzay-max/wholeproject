package com.lzy.lostandfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzy.lostandfound.entity.HonorPeriodItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface HonorPeriodItemMapper extends BaseMapper<HonorPeriodItem> {

    @Update("UPDATE honor_period_item SET is_deleted = 0 WHERE id = #{id} AND is_deleted = 1")
    int restoreById(@Param("id") String id);

    @Update("UPDATE honor_period_item SET is_deleted = 0 WHERE period_id = #{periodId} AND is_deleted = 1")
    int restoreByPeriodId(@Param("periodId") String periodId);
}

