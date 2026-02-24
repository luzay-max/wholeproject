package com.lzy.lostandfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzy.lostandfound.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 评论表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}
