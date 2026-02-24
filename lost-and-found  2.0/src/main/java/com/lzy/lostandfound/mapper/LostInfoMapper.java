package com.lzy.lostandfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzy.lostandfound.entity.LostInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 失物信息表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
@Mapper
public interface LostInfoMapper extends BaseMapper<LostInfo> {

}
