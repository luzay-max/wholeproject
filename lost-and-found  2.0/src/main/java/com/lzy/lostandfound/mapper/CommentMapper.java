package com.lzy.lostandfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzy.lostandfound.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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

    @Update("UPDATE comment SET is_deleted = 0 WHERE id = #{id} AND is_deleted = 1")
    int restoreById(@Param("id") String id);

    @Update({
            "<script>",
            "UPDATE comment",
            "SET is_deleted = 0",
            "WHERE is_deleted = 1",
            "AND id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int restoreBatchByIds(@Param("ids") List<String> ids);
}
