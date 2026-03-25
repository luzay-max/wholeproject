package com.lzy.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 招领信息表
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
@TableName("find_info")
public class FindInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 信息ID
     */
    private String id;

    /**
     * 发布用户ID
     */
    private String userId;

    /**
     * 物品名称
     */
    private String name;

    /**
     * 物品类型
     */
    private String type;

    /**
     * 发现地点
     */
    private String location;

    /**
     * 物品描述
     */
    private String description;

    /**
     * 图片URL，JSON数组格式
     */
    private String images;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 联系人
     */
    private String contactInfo;

    /**
     * 捡到时间
     */
    private LocalDateTime foundTime;

    /**
     * 状态（PENDING/APPROVED/REJECTED/SOLVED）
     */
    private String status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public LocalDateTime getFoundTime() {
        return foundTime;
    }

    public void setFoundTime(LocalDateTime foundTime) {
        this.foundTime = foundTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "FindInfo{" +
            "id = " + id +
            ", userId = " + userId +
            ", name = " + name +
            ", type = " + type +
            ", location = " + location +
            ", description = " + description +
            ", images = " + images +
            ", contactPhone = " + contactPhone +
            ", contactEmail = " + contactEmail +
            ", status = " + status +
            ", rejectReason = " + rejectReason +
            ", viewCount = " + viewCount +
            ", publishTime = " + publishTime +
            ", updateTime = " + updateTime +
            "}";
    }
}
