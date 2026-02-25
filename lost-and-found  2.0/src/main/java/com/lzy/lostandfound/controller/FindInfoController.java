package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.dto.FindInfoCreateRequest;
import com.lzy.lostandfound.dto.FindInfoUpdateRequest;
import com.lzy.lostandfound.entity.Activities;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.service.IActivitiesService;
import com.lzy.lostandfound.service.IFindInfoService;
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
 * 招领信息表 前端控制器
 * @author lzy
 * @since 2025-10-16
 */

@RestController
@RequestMapping("/find")
public class FindInfoController {

    @Autowired
    private IFindInfoService findInfoService;
    @Autowired
    private IActivitiesService activitiesService;

    /**
     * 发布招领信息
     */
    @PostMapping("/publish")
    @Log("PUBLISH_FIND")
    public Result addFindInfo(@Valid @RequestBody FindInfoCreateRequest request) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            String userId = map.get("id").toString();

            FindInfo findInfo = new FindInfo();
            findInfo.setId(UUID.randomUUID().toString());
            findInfo.setUserId(userId);
            findInfo.setName(request.getName());
            findInfo.setType(request.getType());
            findInfo.setLocation(request.getLocation());
            findInfo.setDescription(request.getDescription());
            findInfo.setImages(request.getImages());
            findInfo.setContactInfo(request.getContactName());
            findInfo.setContactPhone(request.getContactPhone());
            findInfo.setContactEmail(request.getContactEmail());
            findInfo.setStatus("PENDING");
            findInfo.setViewCount(0);
            if (request.getFoundTime() != null && !request.getFoundTime().isBlank()) {
                try {
                    long ts = Long.parseLong(request.getFoundTime());
                    LocalDateTime ft = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("Asia/Shanghai"));
                    if (ft.isAfter(LocalDateTime.now(ZoneId.of("Asia/Shanghai")))) {
                        return Result.error("拾取时间不能晚于当前时间");
                    }
                    findInfo.setFoundTime(ft);
                } catch (NumberFormatException ex) {
                    return Result.error("拾取时间格式不正确");
                }
            }
            findInfo.setPublishTime(LocalDateTime.now());
            findInfo.setUpdateTime(LocalDateTime.now());

            findInfoService.save(findInfo);

            return Result.success("发布成功，等待审核");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发布失败，请稍后重试");
        }
    }

 

    /**
     * 查询招领信息列表（分页、可搜索）
     */
    @Cacheable(cacheNames = "FindListCache",key =  "#page + ':' + #pageSize + ':' + #type + ':' + #keyword + ':' + #status + ':' + #startDate + ':' + #endDate")
    @GetMapping("/list")
    public Result getFindList(@RequestParam(defaultValue = "1") Integer page,
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
            LambdaQueryWrapper<FindInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FindInfo::getStatus, status);

            if (type != null && !type.isEmpty()) {
                queryWrapper.eq(FindInfo::getType, type);
            }

            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(q -> q.like(FindInfo::getName, keyword)
                        .or()
                        .like(FindInfo::getDescription, keyword));
            }
            if (startDate != null && endDate != null) {
                ZoneId zoneId = ZoneId.of("Asia/Shanghai"); // 固定时区，避免跨环境偏差
                LocalDateTime startDt = Instant.ofEpochMilli(startDate)
                        .atZone(zoneId)
                        .toLocalDateTime();
                LocalDateTime endDt = Instant.ofEpochMilli(endDate)
                        .atZone(zoneId)
                        .toLocalDateTime();
                queryWrapper.between(FindInfo::getPublishTime, startDt, endDt); // 发布时间在 [startDt, endDt] 之间
            }




            queryWrapper.orderByDesc(FindInfo::getPublishTime);

            Page<FindInfo> pageInfo = findInfoService.page(new Page<>(page, pageSize), queryWrapper);

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
     * 搜索招领信息（兼容前端 /find/search）
     */
    @GetMapping("/search")
    public Result searchFindList(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "8") Integer pageSize,
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false, defaultValue = "APPROVED") String status,
                                 @RequestParam(required = false) Long startDate,
                                 @RequestParam(required = false) Long endDate) {
        return getFindList(page, pageSize, type, keyword, status, startDate, endDate);
    }

    /**
     * 获取招领详情
     */
    @GetMapping({"/{id}", "/detail/{id}"})
    public Result getFindDetail(@PathVariable String id) {
        try {
            FindInfo findInfo = findInfoService.getById(id);
            if (findInfo == null) {
                return Result.error("信息不存在");
            }

            // 增加浏览次数
            findInfo.setViewCount(findInfo.getViewCount() + 1);
            findInfo.setUpdateTime(LocalDateTime.now());
            findInfoService.updateById(findInfo);

            return Result.success(findInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取详情失败");
        }
    }

    /**
     * 获取热门招领信息（前10条）
     */
    @GetMapping("/hot")
    public Result getHotFind() {
        try {
            LambdaQueryWrapper<FindInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FindInfo::getStatus, "APPROVED")
                    .orderByDesc(FindInfo::getViewCount)
                    .last("LIMIT 10");

            return Result.success(findInfoService.list(queryWrapper));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取热门信息失败");
        }
    }

    /**
     * 更新招领信息
     */
    @PutMapping
    @Log("UPDATE_FIND")
    @CacheEvict(cacheNames = "FindListCache", allEntries = true)
    public Result updateFindInfo(@Valid @RequestBody FindInfoUpdateRequest request) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            String userId = map.get("id").toString();

            FindInfo oldFindInfo = findInfoService.getById(request.getId());
            if (oldFindInfo == null) {
                return Result.error("信息不存在");
            }

            if (!oldFindInfo.getUserId().equals(userId)) {
                return Result.forbidden("no permission to update");
            }

            FindInfo findInfo = new FindInfo();
            findInfo.setId(request.getId());
            findInfo.setUserId(userId);
            findInfo.setName(request.getName());
            findInfo.setType(request.getType());
            findInfo.setLocation(request.getLocation());
            findInfo.setDescription(request.getDescription());
            findInfo.setImages(request.getImages());
            findInfo.setContactInfo(request.getContactName());
            findInfo.setContactPhone(request.getContactPhone());
            findInfo.setContactEmail(request.getContactEmail());
            findInfo.setStatus("PENDING");
            findInfo.setViewCount(oldFindInfo.getViewCount());
            findInfo.setPublishTime(oldFindInfo.getPublishTime());
            if (request.getFoundTime() != null && !request.getFoundTime().isBlank()) {
                try {
                    long ts = Long.parseLong(request.getFoundTime());
                    LocalDateTime ft = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("Asia/Shanghai"));
                    if (ft.isAfter(LocalDateTime.now(ZoneId.of("Asia/Shanghai")))) {
                        return Result.error("拾取时间不能晚于当前时间");
                    }
                    findInfo.setFoundTime(ft);
                } catch (NumberFormatException ex) {
                    return Result.error("拾取时间格式不正确");
                }
            }
            findInfo.setUpdateTime(LocalDateTime.now());

            findInfoService.updateById(findInfo);
            return Result.success("更新成功，等待审核");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败");
        }
    }

    /**
     * 删除招领信息
     */
    @DeleteMapping("/{id}")
    @Log("DELETE_FIND")
    @CacheEvict(cacheNames = "FindListCache", allEntries = true)
    public Result deleteFindInfo(@PathVariable String id) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null || map.get("id") == null) {
                return Result.forbidden("未登录或登录信息失效");
            }
            String userId = map.get("id").toString();

            FindInfo findInfo = findInfoService.getById(id);
            if (findInfo == null) {
                return Result.error("信息不存在");
            }

            if (!findInfo.getUserId().equals(userId)) {
                return Result.forbidden("无权限删除此信息");
            }

            findInfoService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    /**
     * 获取用户发布的招领信息（分页）
     */
    @GetMapping({"/user/list", "/userList"})
    public Result getUserFindList(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "8") Integer pageSize) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null || map.get("id") == null) {
                return Result.forbidden("未登录或登录信息失效");
            }
            String userId = map.get("id").toString();

            LambdaQueryWrapper<FindInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FindInfo::getUserId, userId)
                    .orderByDesc(FindInfo::getPublishTime);

            Page<FindInfo> pageInfo = findInfoService.page(new Page<>(page, pageSize), queryWrapper);

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
     * 更新状态并记录活动日志
     */
    @PostMapping("/updateStatus")
    @Log("UPDATE_FIND_STATUS")
    @CacheEvict(cacheNames = "FindListCache", allEntries = true)
    public Result updateStatus(@Valid @RequestBody Status status) {
        try {
            FindInfo findInfo = findInfoService.getById(status.getId());
            if (findInfo == null) {
                return Result.error("信息不存在");
            }

            String normalizedStatus = status.getStatus() != null ? status.getStatus().toUpperCase() : null;
            findInfo.setStatus(normalizedStatus);
            findInfo.setUpdateTime(LocalDateTime.now());
            findInfoService.updateById(findInfo);

            // ✅ 记录用户活动日志
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map != null && map.get("id") != null) {
                String userId = map.get("id").toString();
                Activities act = new Activities();
                act.setId(UUID.randomUUID().toString());
                act.setUserId(userId);
                act.setAction("UPDATE_STATUS");
                act.setItemType("find");
                act.setItemId(findInfo.getId());
                act.setContent("招领物品:" + findInfo.getName() +"已认领");
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
