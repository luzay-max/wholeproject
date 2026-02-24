package com.lzy.lostandfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzy.lostandfound.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评论点赞表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

}
