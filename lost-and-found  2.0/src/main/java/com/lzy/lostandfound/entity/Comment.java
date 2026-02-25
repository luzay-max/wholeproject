package com.lzy.lostandfound.entity;


import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

/**
 * <p>
 * 评论表
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private String id;

    /**
     * 关联信息ID
     */
    private String infoId;

    /**
     * 信息类型（lost/find）
     */
    private String infoType;

    /**
     * 评论用户ID
     */
    private String userId;

    //用户名称
    private String username;

    @TableField(exist = false)
    private String accountName;

    public String getUsername() {
        return username;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    //用户头像
    private String avatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 逻辑删除（0未删 1已删）
     */
    @TableLogic
    private Integer isDeleted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInfoId() {
        return infoId;
    }

    public void setInfoId(String infoId) {
        this.infoId = infoId;
    }

    public String getInfoType() {
        return infoType;
    }

    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "Comment{" +
            "id = " + id +
            ", infoId = " + infoId +
            ", infoType = " + infoType +
            ", userId = " + userId +
            ", content = " + content +
            ", likeCount = " + likeCount +
            ", createTime = " + createTime +
                ", isDeleted = " + isDeleted +
                ", userName = " + username +
                ", avatar = " + avatar +
            "}";
    }
}
