package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.dto.LostInfoCreateRequest;
import com.lzy.lostandfound.dto.LostInfoUpdateRequest;
import com.lzy.lostandfound.entity.Activities;
import com.lzy.lostandfound.entity.LostInfo;
import com.lzy.lostandfound.service.IActivitiesService;
import com.lzy.lostandfound.service.ILostInfoService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.vo.Status;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/**
 * 失物信息表 前端控制器
 */
@RestController
@RequestMapping("/lost")
public class LostInfoController {

    @Autowired
    private ILostInfoService lostInfoService;

    @Autowired
    private IActivitiesService activitiesService; // ✅ 新增注入活动服务

    /**
     * 发布失物信息
     */
    @PostMapping({"", "/publish"})
    @CacheEvict(cacheNames = "LostListCache", allEntries = true)
    @Log("PUBLISH_LOST")
    public Result addLostInfo(@Valid @RequestBody LostInfoCreateRequest request) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            String userId = map.get("id").toString();

            LostInfo lostInfo = new LostInfo();
            lostInfo.setId(UUID.randomUUID().toString());
            lostInfo.setUserId(userId);
            lostInfo.setName(request.getName());
            lostInfo.setType(request.getType());
            lostInfo.setLocation(request.getLocation());
            lostInfo.setDescription(request.getDescription());
            lostInfo.setImages(request.getImages());
            lostInfo.setContactInfo(request.getContactName());
            lostInfo.setContactPhone(request.getContactPhone());
            lostInfo.setContactEmail(request.getContactEmail());
            lostInfo.setStatus("PENDING");
            lostInfo.setViewCount(0);
            try {
                long ts = Long.parseLong(request.getLostTime());
                LocalDateTime lt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("Asia/Shanghai"));
                if (lt.isAfter(LocalDateTime.now(ZoneId.of("Asia/Shanghai")))) {
                    return Result.error("lostTime cannot be in the future");
                }
                lostInfo.setLostTime(lt);
            } catch (NumberFormatException ex) {
                return Result.error("invalid lostTime format");
            }
            lostInfo.setPublishTime(LocalDateTime.now());
            lostInfo.setUpdateTime(LocalDateTime.now());

            lostInfoService.save(lostInfo);



            return Result.success("发布成功，等待审核");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发布失败，请稍后重试");
        }
    }

    /**
     * 分页获取已审核通过的失物列表
     */
    @GetMapping("/list")
    @Cacheable(cacheNames = "LostListCache",key =  "#page + ':' + #pageSize + ':' + #type + ':' + #keyword + ':' + #status + ':' + #startDate + ':' + #endDate")
    public Result getLostList(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "8") Integer pageSize,
                              @RequestParam(required = false) String type,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false,defaultValue = "APPROVED") String status,
                              @RequestParam(required = false) Long startDate,
                              @RequestParam(required = false) Long endDate) {
        try {
            if (status != null) {
                status = status.toUpperCase();
            }
            LambdaQueryWrapper<LostInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LostInfo::getStatus, status);

            if (type != null && !type.isEmpty()) {
                queryWrapper.eq(LostInfo::getType, type);
            }

            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(q -> q.like(LostInfo::getName, keyword)
                        .or()
                        .like(LostInfo::getDescription, keyword));
            }
            if (startDate != null && endDate != null) {
                ZoneId zoneId = ZoneId.of("Asia/Shanghai"); // 固定时区，避免跨环境偏差
                LocalDateTime startDt = Instant.ofEpochMilli(startDate)
                        .atZone(zoneId)
                        .toLocalDateTime();
                LocalDateTime endDt = Instant.ofEpochMilli(endDate)
                        .atZone(zoneId)
                        .toLocalDateTime();
                queryWrapper.between(LostInfo::getPublishTime, startDt, endDt); // 发布时间在 [startDt, endDt] 之间
            }

            queryWrapper.orderByDesc(LostInfo::getPublishTime);

            Page<LostInfo> pageInfo = lostInfoService.page(new Page<>(page, pageSize), queryWrapper);

            return Result.success(Map.of(
                    "list", pageInfo.getRecords(),
                    "total", pageInfo.getTotal()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取列表失败");
        }
    }

    /**
     * 获取详情并增加浏览次数
     */
    @GetMapping({"/{id}", "/detail/{id}"})
    public Result getLostDetail(@PathVariable String id) {
        try {
            LostInfo lostInfo = lostInfoService.getById(id);
            if (lostInfo == null) {
                return Result.error("信息不存在");
            }

            lostInfo.setViewCount(lostInfo.getViewCount() + 1);
            lostInfo.setUpdateTime(LocalDateTime.now());
            lostInfoService.updateById(lostInfo);

            return Result.success(lostInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取详情失败");
        }
    }

    /**
     * 热门信息（前10条）
     */
    @GetMapping("/hot")
    public Result getHotLost() {
        try {
            LambdaQueryWrapper<LostInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LostInfo::getStatus, "APPROVED")
                    .orderByDesc(LostInfo::getViewCount)
                    .last("LIMIT 10");

            return Result.success(lostInfoService.list(queryWrapper));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取热门信息失败");
        }
    }

    /**
     * 修改失物信息
     */
    @PutMapping
    @CacheEvict(cacheNames = "LostListCache", allEntries = true)
    @Log("UPDATE_LOST")
    public Result updateLostInfo(@Valid @RequestBody LostInfoUpdateRequest request) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            String userId = map.get("id").toString();

            LostInfo oldLostInfo = lostInfoService.getById(request.getId());
            if (oldLostInfo == null) {
                return Result.error("info not found");
            }

            if (!oldLostInfo.getUserId().equals(userId)) {
                return Result.forbidden("no permission to update");
            }

            LostInfo lostInfo = new LostInfo();
            lostInfo.setId(request.getId());
            lostInfo.setUserId(userId);
            lostInfo.setName(request.getName());
            lostInfo.setType(request.getType());
            lostInfo.setLocation(request.getLocation());
            lostInfo.setDescription(request.getDescription());
            lostInfo.setImages(request.getImages());
            lostInfo.setContactInfo(request.getContactName());
            lostInfo.setContactPhone(request.getContactPhone());
            lostInfo.setContactEmail(request.getContactEmail());
            lostInfo.setStatus("PENDING");
            lostInfo.setViewCount(oldLostInfo.getViewCount());
            lostInfo.setPublishTime(oldLostInfo.getPublishTime());
            try {
                long ts = Long.parseLong(request.getLostTime());
                LocalDateTime lt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("Asia/Shanghai"));
                if (lt.isAfter(LocalDateTime.now(ZoneId.of("Asia/Shanghai")))) {
                    return Result.error("lostTime cannot be in the future");
                }
                lostInfo.setLostTime(lt);
            } catch (NumberFormatException ex) {
                return Result.error("invalid lostTime format");
            }
            lostInfo.setUpdateTime(LocalDateTime.now());

            lostInfoService.updateById(lostInfo);
            return Result.success("update success, waiting for review");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("update failed");
        }
    }

    /**
     * 删除信息
     */
    @DeleteMapping("/{id}")
    @Log("DELETE_LOST")
    @CacheEvict(cacheNames = "LostListCache", allEntries = true)
    public Result deleteLostInfo(@PathVariable String id) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            String userId = map.get("id").toString();

            LostInfo lostInfo = lostInfoService.getById(id);
            if (lostInfo == null) {
                return Result.error("信息不存在");
            }

            if (!lostInfo.getUserId().equals(userId)) {
                return Result.forbidden("无权限删除此信息");
            }

            lostInfoService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    /**
     * 获取当前用户发布的失物信息
     */
    @GetMapping({"/user/list", "/userList"})
    public Result getUserLostList(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "8") Integer pageSize) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            String userId = map.get("id").toString();

            LambdaQueryWrapper<LostInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LostInfo::getUserId, userId)
                    .orderByDesc(LostInfo::getPublishTime);

            Page<LostInfo> pageInfo = lostInfoService.page(new Page<>(page, pageSize), queryWrapper);

            return Result.success(Map.of(
                    "list", pageInfo.getRecords(),
                    "total", pageInfo.getTotal()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户发布信息失败");
        }
    }

    /**
     * 更新状态并写入活动日志
     */
    @PostMapping("/updateStatus")
    @Log("UPDATE_LOST_STATUS")
    @CacheEvict(cacheNames = "LostListCache", allEntries = true)
    @Transactional
    public Result updateStatus(@Valid @RequestBody Status status) {
        try {
            LostInfo lostInfo = lostInfoService.getById(status.getId());
            if (lostInfo == null) {
                return Result.error("信息不存在");
            }

            String normalizedStatus = status.getStatus() != null ? status.getStatus().toUpperCase() : null;
            lostInfo.setStatus(normalizedStatus);
            lostInfo.setUpdateTime(LocalDateTime.now());
            lostInfoService.updateById(lostInfo);

            // ✅ 插入活动记录
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map != null && map.get("id") != null) {
                String userId = map.get("id").toString();
                Activities act = new Activities();
                act.setId(UUID.randomUUID().toString());
                act.setUserId(userId);
                act.setAction("UPDATE_STATUS");
                act.setItemType("lost");
                act.setItemId(lostInfo.getId());
                act.setContent("失物"+  lostInfo.getName()+"已找回"  );
                act.setCreateTime(LocalDateTime.now());
                activitiesService.save(act);
            }

            return Result.success("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新状态失败");
        }
    }
}
