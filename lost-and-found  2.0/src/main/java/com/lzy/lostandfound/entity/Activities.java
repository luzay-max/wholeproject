package com.lzy.lostandfound.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户活动日志表
 * </p>
 *
 * @author baomidou
 * @since 2025-11-05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Activities implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动态ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 操作类型（发布/解决/评论）
     */
    private String action;

    /**
     * 物品类型（lost/find）
     */
    private String itemType;

    /**
     * 物品ID
     */
    private String itemId;

    /**
     * 动态内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action == null ? null : action.trim().toUpperCase();
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType == null ? null : itemType.trim().toLowerCase();
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Activities{" +
            "id = " + id +
            ", userId = " + userId +
            ", action = " + action +
            ", itemType = " + itemType +
            ", itemId = " + itemId +
            ", content = " + content +
            ", createTime = " + createTime +
            "}";
    }
}
