package com.lzy.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 评论点赞表
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
@TableName("comment_like")
public class CommentLike implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点赞记录ID
     */
    private String id;

    /**
     * 评论ID
     */
    private String commentId;

    /**
     * 点赞用户ID
     */
    private String userId;

    /**
     * 点赞时间
     */
    private LocalDateTime createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CommentLike{" +
            "id = " + id +
            ", commentId = " + commentId +
            ", userId = " + userId +
            ", createTime = " + createTime +
            "}";
    }
}
