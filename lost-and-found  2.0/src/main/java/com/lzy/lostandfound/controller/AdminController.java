package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.entity.*;
import com.lzy.lostandfound.service.*;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.HonorBoardItemVO;
import com.lzy.lostandfound.vo.HonorBoardVO;
import com.lzy.lostandfound.vo.IdAndType;
import com.lzy.lostandfound.vo.Result;
import com.lzy.lostandfound.anno.Log;
import com.alibaba.excel.EasyExcel;
import com.lzy.lostandfound.dto.WhitelistImportDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.io.IOException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class AdminController {
    @Autowired
    private IUserService userService;
    @Autowired
    private ILostInfoService lostInfoService;
    @Autowired
    private IFindInfoService findInfoService;
    @Autowired
    private IActivitiesService activitiesService;
    @Autowired
    private ICommentService commentService;
    @Autowired
    private IHonorPeriodService honorPeriodService;
    @Autowired
    private IHonorPeriodItemService honorPeriodItemService;
    @Autowired
    private IStudentWhitelistService studentWhitelistService;

    @GetMapping("/admin/honor/periods")
    public Result honorPeriods(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "8") Integer pageSize,
                               @RequestParam(required = false) Long startTime,
                               @RequestParam(required = false) Long endTime,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String periodType) {
        try {
            Page<HonorPeriod> p = new Page<>(page, pageSize);
            LambdaQueryWrapper<HonorPeriod> wrapper = new LambdaQueryWrapper<>();
            if (periodType != null && !periodType.isEmpty()) {
                wrapper.eq(HonorPeriod::getPeriodType, periodType);
            }
            if (status != null && !status.isEmpty()) {
                wrapper.eq(HonorPeriod::getStatus, status);
            }
            if (startTime != null && endTime != null) {
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                LocalDateTime start = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDateTime();
                LocalDateTime end = Instant.ofEpochMilli(endTime).atZone(zoneId).toLocalDateTime();
                wrapper.between(HonorPeriod::getPeriodStart, start, end);
            }
            wrapper.orderByDesc(HonorPeriod::getPeriodStart);
            Page<HonorPeriod> pageData = honorPeriodService.page(p, wrapper);
            List<Map<String, Object>> list = new ArrayList<>();
            for (HonorPeriod period : pageData.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                map.put("periodId", period.getId());
                map.put("periodType", period.getPeriodType());
                map.put("periodStart", period.getPeriodStart());
                map.put("periodEnd", period.getPeriodEnd());
                map.put("topN", period.getTopN());
                map.put("totalCompletedCount", period.getTotalCompletedCount());
                map.put("status", period.getStatus());
                map.put("sendTime", period.getSendTime());
                map.put("awardTime", period.getAwardTime());
                list.add(map);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", pageData.getTotal());
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取光荣榜周期列表失败");
        }
    }

    @GetMapping("/admin/honor/board/detail")
    public Result honorBoardDetail(@RequestParam String periodId) {
        try {
            HonorPeriod period = honorPeriodService.getById(periodId);
            if (period == null) {
                return Result.error("周期不存在");
            }
            LambdaQueryWrapper<HonorPeriodItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HonorPeriodItem::getPeriodId, periodId);
            wrapper.orderByAsc(HonorPeriodItem::getRank);
            List<HonorPeriodItem> items = honorPeriodItemService.list(wrapper);
            List<HonorBoardItemVO> list = new ArrayList<>();
            for (HonorPeriodItem item : items) {
                HonorBoardItemVO vo = new HonorBoardItemVO();
                vo.setId(item.getId());
                vo.setRank(item.getRank());
                vo.setUserId(item.getUserId());
                vo.setUsername(item.getUsername());
                vo.setName(item.getName());
                vo.setClassName(item.getClassName());
                vo.setDepartmentName(item.getDepartmentName());
                vo.setAvatar(item.getAvatar());
                vo.setCompletedCount(item.getCompletedCount());
                vo.setPoints(item.getPoints());
                vo.setLastCompletedAt(item.getLastCompletedAt());
                list.add(vo);
            }
            HonorBoardVO board = new HonorBoardVO();
            board.setPeriodId(period.getId());
            board.setPeriodType(period.getPeriodType());
            board.setPeriodStart(period.getPeriodStart());
            board.setPeriodEnd(period.getPeriodEnd());
            board.setTopN(period.getTopN());
            board.setTotalCompletedCount(period.getTotalCompletedCount());
            board.setStatus(period.getStatus());
            board.setList(list);
            return Result.success(board);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取光荣榜详情失败");
        }
    }

    @PostMapping("/admin/honor/markSent")
    @Log("MARK_HONOR_SENT")
    public Result markHonorSent(@RequestBody Map<String, String> body) {
        try {
            String periodId = body.get("periodId");
            if (periodId == null || periodId.isEmpty()) {
                return Result.error("periodId不能为空");
            }
            HonorPeriod period = honorPeriodService.getById(periodId);
            if (period == null) {
                return Result.error("周期不存在");
            }
            if (!"SENT".equals(period.getStatus())) {
                period.setStatus("SENT");
                period.setSendTime(LocalDateTime.now());
                honorPeriodService.updateById(period);
            }
            return Result.success("标记成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记已发送失败");
        }
    }

    @PostMapping("/admin/honor/markAwarded")
    @Log("MARK_HONOR_AWARDED")
    public Result markHonorAwarded(@RequestBody Map<String, String> body) {
        try {
            String periodId = body.get("periodId");
            if (periodId == null || periodId.isEmpty()) {
                return Result.error("periodId不能为空");
            }
            HonorPeriod period = honorPeriodService.getById(periodId);
            if (period == null) {
                return Result.error("周期不存在");
            }
            if (!"AWARDED".equals(period.getStatus())) {
                period.setStatus("AWARDED");
                period.setAwardTime(LocalDateTime.now());
                honorPeriodService.updateById(period);
            }
            return Result.success("标记成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记已颁奖失败");
        }
    }

    @PutMapping("/admin/honor/periods/{periodId}")
    @Log("UPDATE_HONOR_PERIOD")
    public Result updateHonorPeriod(@PathVariable String periodId, @RequestBody HonorPeriod period) {
        try {
            HonorPeriod existing = honorPeriodService.getById(periodId);
            if (existing == null) {
                return Result.error("周期不存在");
            }
            
            // 更新允许修改的字段
            if (period.getPeriodStart() != null) existing.setPeriodStart(period.getPeriodStart());
            if (period.getPeriodEnd() != null) existing.setPeriodEnd(period.getPeriodEnd());
            if (period.getStatus() != null) existing.setStatus(period.getStatus());
            // 其他统计字段通常由系统生成，但在手动修正时也允许修改
            if (period.getTotalCompletedCount() != null) existing.setTotalCompletedCount(period.getTotalCompletedCount());
            if (period.getTopN() != null) existing.setTopN(period.getTopN());
            
            honorPeriodService.updateById(existing);
            return Result.success("更新光荣榜周期成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新光荣榜周期失败");
        }
    }

    @GetMapping("/admin/honor/candidates")
    public Result searchHonorCandidates(@RequestParam(required = false) String keyword) {
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            // 过滤掉已封禁的用户（可选，暂时不过滤以便能查到所有人）
            // wrapper.eq(User::getStatus, 0);
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w.like(User::getName, keyword)
                        .or().like(User::getUsername, keyword)
                        .or().like(User::getStudentId, keyword));
            }
            wrapper.last("limit 20"); // 限制返回数量，防止数据过多
            List<User> users = userService.list(wrapper);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", user.getId());
                map.put("username", user.getUsername());
                map.put("name", user.getName());
                map.put("departmentName", user.getCollege());
                map.put("avatar", user.getAvatar());
                map.put("studentId", user.getStudentId());
                result.add(map);
            }
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("搜索候选人失败");
        }
    }

    @PostMapping("/admin/honor/items/batch")
    @Log("BATCH_ADD_HONOR_ITEMS")
    public Result batchAddHonorPeriodItems(@RequestBody Map<String, Object> body) {
        try {
            String periodId = (String) body.get("periodId");
            List<String> userIds = (List<String>) body.get("userIds");
            
            if (periodId == null || userIds == null || userIds.isEmpty()) {
                return Result.error("参数错误");
            }
            
            HonorPeriod period = honorPeriodService.getById(periodId);
            if (period == null) {
                return Result.error("周期不存在");
            }
            
            // 获取当前最大排名
            LambdaQueryWrapper<HonorPeriodItem> query = new LambdaQueryWrapper<>();
            query.eq(HonorPeriodItem::getPeriodId, periodId);
            query.orderByDesc(HonorPeriodItem::getRank);
            query.last("limit 1");
            HonorPeriodItem lastItem = honorPeriodItemService.getOne(query);
            int startRank = (lastItem != null && lastItem.getRank() != null) ? lastItem.getRank() + 1 : 1;
            
            List<HonorPeriodItem> newItems = new ArrayList<>();
            int addedCount = 0;
            
            for (String userId : userIds) {
                // 检查是否已存在
                LambdaQueryWrapper<HonorPeriodItem> dupCheck = new LambdaQueryWrapper<>();
                dupCheck.eq(HonorPeriodItem::getPeriodId, periodId);
                dupCheck.eq(HonorPeriodItem::getUserId, userId);
                if (honorPeriodItemService.count(dupCheck) > 0) {
                    continue;
                }
                
                User user = userService.getById(userId);
                if (user == null) continue;
                
                HonorPeriodItem item = new HonorPeriodItem();
                item.setId(UUID.randomUUID().toString());
                item.setPeriodId(periodId);
                item.setUserId(userId);
                item.setRank(startRank + addedCount);
                item.setUsername(user.getUsername());
                item.setName(user.getName());
                item.setDepartmentName(user.getCollege());
                // className 暂时为空，User表中无此字段
                item.setAvatar(user.getAvatar());
                item.setCompletedCount(0);
                item.setPoints(0);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                
                newItems.add(item);
                addedCount++;
            }
            
            if (!newItems.isEmpty()) {
                honorPeriodItemService.saveBatch(newItems);
            }
            
            return Result.success("批量添加成功，新增 " + newItems.size() + " 条记录");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量添加失败: " + e.getMessage());
        }
    }

    @PostMapping("/admin/honor/items")
    @Log("ADD_HONOR_ITEM")
    public Result addHonorPeriodItem(@RequestBody HonorPeriodItem item) {
        try {
            if (item.getPeriodId() == null || item.getPeriodId().isEmpty()) {
                return Result.error("periodId不能为空");
            }
            
            // 如果没有ID，生成一个
            if (item.getId() == null || item.getId().isEmpty()) {
                item.setId(UUID.randomUUID().toString());
            }
            
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            
            boolean saved = honorPeriodItemService.save(item);
            if (saved) {
                return Result.success("添加榜单成员成功");
            } else {
                return Result.error("添加榜单成员失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加榜单成员失败: " + e.getMessage());
        }
    }

    @PutMapping("/admin/honor/items/{id}")
    @Log("UPDATE_HONOR_ITEM")
    public Result updateHonorPeriodItem(@PathVariable String id, @RequestBody HonorPeriodItem item) {
        try {
            HonorPeriodItem existing = honorPeriodItemService.getById(id);
            if (existing == null) {
                return Result.error("榜单成员不存在");
            }
            
            // 更新字段
            if (item.getRank() != null) existing.setRank(item.getRank());
            if (item.getUsername() != null) existing.setUsername(item.getUsername());
            if (item.getName() != null) existing.setName(item.getName());
            if (item.getClassName() != null) existing.setClassName(item.getClassName());
            if (item.getDepartmentName() != null) existing.setDepartmentName(item.getDepartmentName());
            if (item.getCompletedCount() != null) existing.setCompletedCount(item.getCompletedCount());
            if (item.getPoints() != null) existing.setPoints(item.getPoints());
            if (item.getLastCompletedAt() != null) existing.setLastCompletedAt(item.getLastCompletedAt());
            
            existing.setUpdatedAt(LocalDateTime.now());
            
            boolean updated = honorPeriodItemService.updateById(existing);
            if (updated) {
                return Result.success("更新榜单成员成功");
            } else {
                return Result.error("更新榜单成员失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新榜单成员失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/honor/items/{id}")
    public Result deleteHonorPeriodItem(@PathVariable String id) {
        try {
            boolean removed = honorPeriodItemService.removeById(id);
            if (removed) {
                return Result.success("删除榜单成员成功");
            } else {
                return Result.error("删除榜单成员失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除榜单成员失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/honor/periods/{periodId}")
    @Log("DELETE_HONOR_PERIOD")
    public Result deleteHonorPeriod(@PathVariable String periodId) {
        try {
            if (periodId == null || periodId.isEmpty()) {
                return Result.error("periodId不能为空");
            }
            
            // 检查周期是否存在
            HonorPeriod period = honorPeriodService.getById(periodId);
            if (period == null) {
                return Result.error("周期不存在");
            }
            
            // 先删除关联的榜单项
            LambdaQueryWrapper<HonorPeriodItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(HonorPeriodItem::getPeriodId, periodId);
            honorPeriodItemService.remove(itemWrapper);
            
            // 再删除周期本身
            boolean deleted = honorPeriodService.removeById(periodId);
            if (deleted) {
                return Result.success("删除光荣榜成功");
            } else {
                return Result.error("删除光荣榜失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除光荣榜失败：" + e.getMessage());
        }
    }

    @GetMapping("/audit/list")
    public Result getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String type, // lost / find / all
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        try {
            if (status != null) {
                status = status.toUpperCase();
            }
            ZoneId zoneId = ZoneId.of("Asia/Shanghai");
            LocalDateTime startDt = null, endDt = null;
            if (startTime != null && endTime != null) {
                startDt = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDateTime();
                endDt = Instant.ofEpochMilli(endTime).atZone(zoneId).toLocalDateTime();
            }

            // 结果列表
            List<Map<String, Object>> mergedList = new ArrayList<>();
            int total = 0;

            // ========== 查询失物 ==========
            if (!"find".equalsIgnoreCase(type)) {
                LambdaQueryWrapper<LostInfo> lostWrapper = new LambdaQueryWrapper<>();
                lostWrapper.eq(LostInfo::getStatus, status);
                if (keyword != null && !keyword.isEmpty()) {
                    lostWrapper.and(q -> q.like(LostInfo::getName, keyword)
                            .or().like(LostInfo::getDescription, keyword));
                }
                if (startDt != null && endDt != null) {
                    lostWrapper.between(LostInfo::getPublishTime, startDt, endDt);
                }
                lostWrapper.orderByDesc(LostInfo::getPublishTime);

                // 获取失物总数
                total += lostInfoService.count(lostWrapper);
                
                // 转为统一结构
                List<LostInfo> lostList = lostInfoService.list(lostWrapper);
                for (LostInfo info : lostList) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", info.getId());
                    item.put("name", info.getName());
                    item.put("description", info.getDescription());
                    item.put("status", info.getStatus());
                    item.put("publishTime", info.getPublishTime());
                    item.put("type", "lost"); // 区分来源
                    item.put("location", info.getLocation());
                    item.put("images", info.getImages());
                    item.put("contactPhone", info.getContactPhone());
                    item.put("contactEmail", info.getContactEmail());
                    item.put("contactInfo", info.getContactInfo());
                    item.put("lostTime", info.getLostTime());
                    mergedList.add(item);
                }
            }

            // ========== 查询招领 ==========
            if (!"lost".equalsIgnoreCase(type)) {
                LambdaQueryWrapper<com.lzy.lostandfound.entity.FindInfo> findWrapper = new LambdaQueryWrapper<>();
                findWrapper.eq(com.lzy.lostandfound.entity.FindInfo::getStatus, status);
                if (keyword != null && !keyword.isEmpty()) {
                    findWrapper.and(q -> q.like(com.lzy.lostandfound.entity.FindInfo::getName, keyword)
                            .or().like(com.lzy.lostandfound.entity.FindInfo::getDescription, keyword));
                }
                if (startDt != null && endDt != null) {
                    findWrapper.between(com.lzy.lostandfound.entity.FindInfo::getPublishTime, startDt, endDt);
                }
                findWrapper.orderByDesc(com.lzy.lostandfound.entity.FindInfo::getPublishTime);
                
                // 获取招领总数
                total += findInfoService.count(findWrapper);
                
                // 转为统一结构
                List<com.lzy.lostandfound.entity.FindInfo> findList = findInfoService.list(findWrapper);
                for (com.lzy.lostandfound.entity.FindInfo info : findList) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", info.getId());
                    item.put("name", info.getName());
                    item.put("description", info.getDescription());
                    item.put("status", info.getStatus());
                    item.put("publishTime", info.getPublishTime());
                    item.put("type", "find");
                    item.put("location", info.getLocation());
                    item.put("images", info.getImages());
                    item.put("contactPhone", info.getContactPhone());
                    item.put("contactEmail", info.getContactEmail());
                    item.put("contactInfo", info.getContactInfo());
                    item.put("foundTime", info.getFoundTime());
                    mergedList.add(item);
                }
            }

            // ========== 统一排序（按时间降序） ==========
            mergedList.sort((a, b) -> {
                LocalDateTime t1 = (LocalDateTime) a.get("publishTime");
                LocalDateTime t2 = (LocalDateTime) b.get("publishTime");
                return t2.compareTo(t1);
            });

            // ========== 分页处理 ==========
            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, mergedList.size());
            List<Map<String, Object>> pagedList =
                    fromIndex >= mergedList.size() ? Collections.emptyList() : mergedList.subList(fromIndex, toIndex);

            return Result.success(Map.of(
                    "list", pagedList,
                    "total", total
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取审核列表失败");
        }
    }

    @PostMapping("/audit/pass")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    @Log("AUDIT")
    public Result pass(@Valid @RequestBody IdAndType idAndType){

        if ("lost".equalsIgnoreCase(idAndType.getType())) {
            LostInfo lostInfo = lostInfoService.getById(idAndType.getId());
            lostInfo.setStatus("APPROVED");

            lostInfoService.updateById(lostInfo);
            // 插入活动记录
            Activities act = new Activities();
            act.setId(UUID.randomUUID().toString());
            act.setUserId(lostInfo.getUserId());
            act.setAction("PUBLISH");
            act.setItemType("lost");
            act.setItemId(lostInfo.getId());
            act.setContent("发布了失物信息：" + lostInfo.getName());
            act.setCreateTime(LocalDateTime.now());
            activitiesService.save(act);

        } else if ("find".equalsIgnoreCase(idAndType.getType())) {
            com.lzy.lostandfound.entity.FindInfo findInfo = findInfoService.getById(idAndType.getId());
            findInfo.setStatus("APPROVED");
            findInfoService.updateById(findInfo);
            //  插入审核通过后发布活动日志
            Activities act = new Activities();
            act.setId(UUID.randomUUID().toString());
            act.setUserId(findInfo.getUserId());
            act.setAction("PUBLISH");
            act.setItemType("find");
            act.setItemId(findInfo.getId());
            act.setContent("发布了招领信息：" + findInfo.getName());
            act.setCreateTime(LocalDateTime.now());
            activitiesService.save(act);
        } else {
            return Result.error("类型错误");
        }
        return Result.success();
    }

    @PostMapping("/audit/hide")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    @Log("AUDIT")
    public Result hide(@Valid @RequestBody IdAndType idAndType){
        if ("lost".equalsIgnoreCase(idAndType.getType())) {
            LostInfo lostInfo = lostInfoService.getById(idAndType.getId());
            if (lostInfo == null) {
                return Result.error("信息不存在");
            }
            lostInfo.setStatus("HIDDEN");
            lostInfoService.updateById(lostInfo);
            Activities act = new Activities();
            act.setId(UUID.randomUUID().toString());
            act.setUserId(lostInfo.getUserId());
            act.setAction("HIDE");
            act.setItemType("lost");
            act.setItemId(lostInfo.getId());
            act.setContent("管理员下架了失物信息：" + lostInfo.getName());
            act.setCreateTime(LocalDateTime.now());
            activitiesService.save(act);
        } else if ("find".equalsIgnoreCase(idAndType.getType())) {
            com.lzy.lostandfound.entity.FindInfo findInfo = findInfoService.getById(idAndType.getId());
            if (findInfo == null) {
                return Result.error("信息不存在");
            }
            findInfo.setStatus("HIDDEN");
            findInfoService.updateById(findInfo);
            Activities act = new Activities();
            act.setId(UUID.randomUUID().toString());
            act.setUserId(findInfo.getUserId());
            act.setAction("HIDE");
            act.setItemType("find");
            act.setItemId(findInfo.getId());
            act.setContent("管理员下架了招领信息：" + findInfo.getName());
            act.setCreateTime(LocalDateTime.now());
            activitiesService.save(act);
        } else {
            return Result.error("类型错误");
        }
        return Result.success();
    }

    @PostMapping("/audit/recover")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    @Log("AUDIT")
    public Result recover(@Valid @RequestBody IdAndType idAndType){
        if ("lost".equalsIgnoreCase(idAndType.getType())) {
            LostInfo lostInfo = lostInfoService.getById(idAndType.getId());
            if (lostInfo == null) {
                return Result.error("信息不存在");
            }
            lostInfo.setStatus("PENDING");
            lostInfoService.updateById(lostInfo);
            Activities act = new Activities();
            act.setId(UUID.randomUUID().toString());
            act.setUserId(lostInfo.getUserId());
            act.setAction("RECOVER");
            act.setItemType("lost");
            act.setItemId(lostInfo.getId());
            act.setContent("管理员恢复至待审核：失物 " + lostInfo.getName());
            act.setCreateTime(LocalDateTime.now());
            activitiesService.save(act);
        } else if ("find".equalsIgnoreCase(idAndType.getType())) {
            com.lzy.lostandfound.entity.FindInfo findInfo = findInfoService.getById(idAndType.getId());
            if (findInfo == null) {
                return Result.error("信息不存在");
            }
            findInfo.setStatus("PENDING");
            findInfoService.updateById(findInfo);
            Activities act = new Activities();
            act.setId(UUID.randomUUID().toString());
            act.setUserId(findInfo.getUserId());
            act.setAction("RECOVER");
            act.setItemType("find");
            act.setItemId(findInfo.getId());
            act.setContent("管理员恢复至待审核：招领 " + findInfo.getName());
            act.setCreateTime(LocalDateTime.now());
            activitiesService.save(act);
        } else {
            return Result.error("类型错误");
        }
        return Result.success();
    }

    @PostMapping("/audit/batchPass")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    @Log("AUDIT")
    public Result batchPass(@Valid @RequestBody List<IdAndType> idAndTypeList) {
        try {
            for (IdAndType idAndType : idAndTypeList) {
                if ("lost".equalsIgnoreCase(idAndType.getType())) {
                    LostInfo lostInfo = lostInfoService.getById(idAndType.getId());
                    if (lostInfo != null) {
                        lostInfo.setStatus("APPROVED");
                        lostInfoService.updateById(lostInfo);
                        Activities act = new Activities();
                        act.setId(UUID.randomUUID().toString());
                        act.setUserId(lostInfo.getUserId());
                        act.setAction("PUBLISH");
                        act.setItemType("lost");
                        act.setItemId(lostInfo.getId());
                        act.setContent("发布了失物信息：" + lostInfo.getName());
                        act.setCreateTime(LocalDateTime.now());
                        activitiesService.save(act);
                    }
                } else if ("find".equalsIgnoreCase(idAndType.getType())) {
                    com.lzy.lostandfound.entity.FindInfo findInfo = findInfoService.getById(idAndType.getId());
                    if (findInfo != null) {
                        findInfo.setStatus("APPROVED");
                        findInfoService.updateById(findInfo);
                        //  插入审核通过后发布活动日志
                        Activities act = new Activities();
                        act.setId(UUID.randomUUID().toString());
                        act.setUserId(findInfo.getUserId());
                        act.setAction("PUBLISH");
                        act.setItemType("find");
                        act.setItemId(findInfo.getId());
                        act.setContent("发布了招领信息：" + findInfo.getName());
                        act.setCreateTime(LocalDateTime.now());
                        activitiesService.save(act);
                    }
                }
            }
            return Result.success("批量通过成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量通过失败");
        }
    }

    @PostMapping("/audit/reject")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    @Log("AUDIT")
    public Result reject(@Valid @RequestBody IdAndType idAndType){
        Map<String, Object> userMap = ThreadLocalUtil.get();

        if ("lost".equalsIgnoreCase(idAndType.getType())) {

            LostInfo lostInfo = lostInfoService.getById(idAndType.getId());
            lostInfo.setStatus("REJECTED");
            //评论区发布平台管理员的评论
            Comment comment = new Comment();
            comment.setInfoId(idAndType.getId());
            comment.setId(UUID.randomUUID().toString());
            comment.setUserId(userMap.get("id").toString());
            comment.setInfoType(idAndType.getType());
            comment.setLikeCount(0);
            comment.setCreateTime(LocalDateTime.now());
            comment.setUsername("管理员");
            comment.setContent(idAndType.getReason());
            commentService.save(comment);
            lostInfoService.updateById(lostInfo);
        } else if ("find".equalsIgnoreCase(idAndType.getType())) {
            com.lzy.lostandfound.entity.FindInfo findInfo = findInfoService.getById(idAndType.getId());
            findInfo.setStatus("REJECTED");
            //评论区发布平台管理员的评论
            Comment comment = new Comment();
            comment.setInfoId(idAndType.getId());
            comment.setId(UUID.randomUUID().toString());
            comment.setUserId(userMap.get("id").toString());
            comment.setInfoType(idAndType.getType());
            comment.setLikeCount(0);
            comment.setCreateTime(LocalDateTime.now());
            comment.setUsername("管理员");
            comment.setContent(idAndType.getReason());
            commentService.save(comment);
            findInfoService.updateById(findInfo);
        } else {
            return Result.error("类型错误");
        }
        return Result.success();
    }

    // ========== 用户管理接口 ==========

    /**
     * 查看用户列表
     */
    @GetMapping("/admin/user/list")
    public Result getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        try {
            Page<User> userPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(q -> q.like(User::getUsername, keyword)
                        .or().like(User::getName, keyword)
                        .or().like(User::getPhone, keyword)
                        .or().like(User::getEmail, keyword));
            }
            
            // 角色筛选
            if (role != null && !role.isEmpty()) {
                wrapper.eq(User::getRole, role);
            }
            
            // 按创建时间降序排序
            wrapper.orderByDesc(User::getCreateTime);
            
            // 执行分页查询
            Page<User> resultPage = userService.page(userPage, wrapper);
            
            return Result.success(Map.of(
                    "list", resultPage.getRecords(),
                    "total", resultPage.getTotal(),
                    "current", resultPage.getCurrent(),
                    "size", resultPage.getSize(),
                    "pages", resultPage.getPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户列表失败");
        }
    }

    /**
     * 设置/取消管理员权限
     */
    @PostMapping("/admin/user/role")
    public Result updateUserRole(@RequestBody Map<String, String> params) {
        try {
            String userId = params.get("userId");
            String role = params.get("role");
            
            if (userId == null || role == null) {
                return Result.error("参数不能为空");
            }
            
            // 验证角色是否合法
            if (!Arrays.asList("STUDENT", "TEACHER", "ADMIN").contains(role)) {
                return Result.error("角色类型不合法");
            }
            
            // 更新用户角色
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            user.setRole(role);
            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);
            
            return Result.success("角色更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("角色更新失败");
        }
    }

    /**
     * 查看用户列表（完整参数版）
     */
    @GetMapping("/admin/users")
    public Result getUsersList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            Page<User> userPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            
            // ID搜索
            if (id != null && !id.isEmpty()) {
                wrapper.eq(User::getId, id);
            }

            // 用户名搜索
            if (username != null && !username.isEmpty()) {
                wrapper.and(q -> q.like(User::getUsername, username)
                        .or().like(User::getName, username));
            }
            
            // 角色筛选
            if (role != null && !role.isEmpty()) {
                wrapper.eq(User::getRole, role);
            }
            
            // 时间范围筛选
            if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                LocalDateTime startDt;
                LocalDateTime endDt;
                boolean startIsNumber = startTime.chars().allMatch(Character::isDigit);
                boolean endIsNumber = endTime.chars().allMatch(Character::isDigit);
                if (startIsNumber && endIsNumber) {
                    long s = Long.parseLong(startTime);
                    long e = Long.parseLong(endTime);
                    startDt = Instant.ofEpochMilli(s).atZone(zoneId).toLocalDateTime();
                    endDt = Instant.ofEpochMilli(e).atZone(zoneId).toLocalDateTime();
                } else {
                    if (startTime.length() == 10) {
                        LocalDate sDate = LocalDate.parse(startTime);
                        startDt = sDate.atStartOfDay();
                    } else {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        startDt = LocalDateTime.parse(startTime, dtf);
                    }
                    if (endTime.length() == 10) {
                        LocalDate eDate = LocalDate.parse(endTime);
                        endDt = eDate.plusDays(1).atStartOfDay().minusNanos(1);
                    } else {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        endDt = LocalDateTime.parse(endTime, dtf);
                    }
                }
                wrapper.between(User::getCreateTime, startDt, endDt);
            }
            
            // 按创建时间降序排序
            wrapper.orderByDesc(User::getCreateTime);
            
            // 执行分页查询
            Page<User> resultPage = userService.page(userPage, wrapper);
            
            return Result.success(Map.of(
                    "list", resultPage.getRecords(),
                    "total", resultPage.getTotal(),
                    "current", resultPage.getCurrent(),
                    "size", resultPage.getSize(),
                    "pages", resultPage.getPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户列表失败");
        }
    }

    /**
     * 新增用户
     */
    @PostMapping("/admin/users")
    @Log("ADD_USER")
    public Result createUser(@RequestBody User user) {
        try {
            // 检查用户名是否已存在
            User existing = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername()));
            if (existing != null) {
                return Result.error("用户名已存在");
            }

            // 校验真实姓名格式
            if (user.getName() != null && !user.getName().matches("^[\\u4e00-\\u9fa5\\u3400-\\u4db5a-zA-Z\\s.·]{2,20}$")) {
                return Result.error("真实姓名格式不正确（2-20位，支持中英文、生僻字、点、空格）");
            }

            // 生成ID
            user.setId(UUID.randomUUID().toString());
            
            // 设置默认密码（如果未提供）或加密密码
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword("123456"); // 默认密码
            }
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            user.setPassword(encoder.encode(user.getPassword()));

            // 设置其他默认值
            if (user.getStatus() == null) {
                user.setStatus(0); // 默认正常
            }
            if (user.getRole() == null) {
                user.setRole("STUDENT"); // 默认学生
            }
            
            // 补充账户名（如果未提供，默认同用户名）
            if (user.getAccountName() == null || user.getAccountName().isEmpty()) {
                user.setAccountName(user.getUsername());
            }

            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            boolean saved = userService.save(user);
            if (saved) {
                return Result.success("新增用户成功");
            } else {
                return Result.error("新增用户失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/admin/users/{id}")
    @Log("UPDATE_USER")
    public Result updateUser(@PathVariable String id, @RequestBody User user) {
        try {
            User existing = userService.getById(id);
            if (existing == null) {
                return Result.error("用户不存在");
            }

            // 校验真实姓名格式
            if (user.getName() != null && !user.getName().matches("^[\\u4e00-\\u9fa5\\u3400-\\u4db5a-zA-Z\\s.·]{2,20}$")) {
                return Result.error("真实姓名格式不正确（2-20位，支持中英文、生僻字、点、空格）");
            }

            // 更新基本信息
            existing.setUsername(user.getUsername());
            existing.setName(user.getName());
            existing.setPhone(user.getPhone());
            existing.setEmail(user.getEmail());
            existing.setRole(user.getRole());
            existing.setCollege(user.getCollege());
            existing.setStudentId(user.getStudentId());
            existing.setStatus(user.getStatus());
            
            // 如果提供了新密码，则加密更新
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                existing.setPassword(encoder.encode(user.getPassword()));
            }

            existing.setUpdateTime(LocalDateTime.now());

            boolean updated = userService.updateById(existing);
            if (updated) {
                return Result.success("更新用户成功");
            } else {
                return Result.error("更新用户失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新用户失败");
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/admin/users/{id}")
    @Log("DELETE_USER")
    public Result deleteUser(@PathVariable String id) {
        try {
            boolean deleted = userService.removeById(id);
            if (deleted) {
                return Result.success("删除用户成功");
            } else {
                return Result.error("删除用户失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除用户失败");
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/admin/users/{userId}/password/reset")
    @Log("RESET_PASSWORD")
    public Result resetUserPassword(@PathVariable String userId) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 默认密码 123456
            String defaultPassword = "123456";
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            user.setPassword(encoder.encode(defaultPassword));
            user.setUpdateTime(LocalDateTime.now());
            
            boolean updated = userService.updateById(user);
            if (updated) {
                return Result.success("密码重置成功，新密码为: " + defaultPassword);
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("密码重置失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/admin/users/batch")
    @Log("BATCH_DELETE_USERS")
    public Result batchDeleteUsers(@RequestBody Map<String, List<String>> params) {
        try {
            List<String> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的用户");
            }
            
            boolean deleted = userService.removeBatchByIds(ids);
            if (deleted) {
                return Result.success("批量删除用户成功");
            } else {
                return Result.error("批量删除用户失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除用户失败");
        }
    }

    /**
     * 查看招领信息列表
     */
    @GetMapping("/admin/find")
    public Result getFindInfoList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        try {
            if (status != null) {
                status = status.toUpperCase();
            }
            Page<com.lzy.lostandfound.entity.FindInfo> findPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<com.lzy.lostandfound.entity.FindInfo> wrapper = new LambdaQueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(q -> q.like(com.lzy.lostandfound.entity.FindInfo::getName, keyword)
                        .or().like(com.lzy.lostandfound.entity.FindInfo::getDescription, keyword));
            }
            
            // 类型筛选（这里的type参数可能是指物品类型，根据实际需求调整）
            // 如果type参数是指物品类型字段，需要根据FindInfo实体类的字段名调整
            
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                wrapper.eq(com.lzy.lostandfound.entity.FindInfo::getStatus, status);
            }
            
            // 时间范围筛选
            if (startTime != null && endTime != null) {
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                LocalDateTime startDt = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDateTime();
                LocalDateTime endDt = Instant.ofEpochMilli(endTime).atZone(zoneId).toLocalDateTime();
                wrapper.between(com.lzy.lostandfound.entity.FindInfo::getPublishTime, startDt, endDt);
            }
            
            // 按发布时间降序排序
            wrapper.orderByDesc(com.lzy.lostandfound.entity.FindInfo::getPublishTime);
            
            // 执行分页查询
            Page<com.lzy.lostandfound.entity.FindInfo> resultPage = findInfoService.page(findPage, wrapper);
            
            return Result.success(Map.of(
                    "list", resultPage.getRecords(),
                    "total", resultPage.getTotal(),
                    "current", resultPage.getCurrent(),
                    "size", resultPage.getSize(),
                    "pages", resultPage.getPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取招领信息列表失败");
        }
    }

    /**
     * 查看失物信息列表
     */
    @GetMapping("/admin/lost")
    public Result getLostInfoList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        try {
            if (status != null) {
                status = status.toUpperCase();
            }
            Page<LostInfo> lostPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<LostInfo> wrapper = new LambdaQueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(q -> q.like(LostInfo::getName, keyword)
                        .or().like(LostInfo::getDescription, keyword));
            }
            
            // 类型筛选（这里的type参数可能是指物品类型，根据实际需求调整）
            // 如果type参数是指物品类型字段，需要根据LostInfo实体类的字段名调整
            
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                wrapper.eq(LostInfo::getStatus, status);
            }
            
            // 时间范围筛选
            if (startTime != null && endTime != null) {
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                LocalDateTime startDt = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDateTime();
                LocalDateTime endDt = Instant.ofEpochMilli(endTime).atZone(zoneId).toLocalDateTime();
                wrapper.between(LostInfo::getPublishTime, startDt, endDt);
            }
            
            // 按发布时间降序排序
            wrapper.orderByDesc(LostInfo::getPublishTime);
            
            // 执行分页查询
            Page<LostInfo> resultPage = lostInfoService.page(lostPage, wrapper);
            
            return Result.success(Map.of(
                    "list", resultPage.getRecords(),
                    "total", resultPage.getTotal(),
                    "current", resultPage.getCurrent(),
                    "size", resultPage.getSize(),
                    "pages", resultPage.getPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失物信息列表失败");
        }
    }

    // ========== 数据统计接口 ==========

    /**
     * 统计总数据
     */
    @GetMapping("/admin/statistics")
    public Result getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总用户数
            long totalUsers = userService.count();
            statistics.put("totalUsers", totalUsers);
            
            // 管理员数量
            long adminCount = userService.count(new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
            statistics.put("adminCount", adminCount);
            
            // 总失物数
            long totalLost = lostInfoService.count();
            statistics.put("totalLost", totalLost);
            
            // 总招领数
            long totalFind = findInfoService.count();
            statistics.put("totalFind", totalFind);
            
            // 总评论数
            long totalComments = commentService.count();
            statistics.put("totalComments", totalComments);
            
            // 待审核失物数
            long pendingLost = lostInfoService.count(new LambdaQueryWrapper<LostInfo>().eq(LostInfo::getStatus, "PENDING"));
            statistics.put("pendingLost", pendingLost);
            
            // 已通过失物数
            long approvedLost = lostInfoService.count(new LambdaQueryWrapper<LostInfo>().eq(LostInfo::getStatus, "APPROVED"));
            statistics.put("approvedLost", approvedLost);
            
            // 已拒绝失物数
            long rejectedLost = lostInfoService.count(new LambdaQueryWrapper<LostInfo>().eq(LostInfo::getStatus, "REJECTED"));
            statistics.put("rejectedLost", rejectedLost);
            
            // 待审核招领数
            long pendingFind = findInfoService.count(new LambdaQueryWrapper<com.lzy.lostandfound.entity.FindInfo>().eq(com.lzy.lostandfound.entity.FindInfo::getStatus, "PENDING"));
            statistics.put("pendingFind", pendingFind);
            
            // 已通过招领数
            long approvedFind = findInfoService.count(new LambdaQueryWrapper<com.lzy.lostandfound.entity.FindInfo>().eq(com.lzy.lostandfound.entity.FindInfo::getStatus, "APPROVED"));
            statistics.put("approvedFind", approvedFind);
            
            // 已拒绝招领数
            long rejectedFind = findInfoService.count(new LambdaQueryWrapper<com.lzy.lostandfound.entity.FindInfo>().eq(com.lzy.lostandfound.entity.FindInfo::getStatus, "REJECTED"));
            statistics.put("rejectedFind", rejectedFind);
            
            // 总活动记录数
            long totalActivities = activitiesService.count();
            statistics.put("totalActivities", totalActivities);
            
            return Result.success(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计数据失败");
        }
    }

    // ========== 评论管理接口 ==========

    /**
     * 查看评论列表
     */
    @GetMapping("/admin/comment/list")
    public Result getCommentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String infoType) {
        try {
            Page<Comment> commentPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(Comment::getContent, keyword);
            }
            
            // 信息类型筛选
            if (infoType != null && !infoType.isEmpty()) {
                wrapper.eq(Comment::getInfoType, infoType);
            }
            
            // 按创建时间降序排序
            wrapper.orderByDesc(Comment::getCreateTime);
            
            // 执行分页查询
            Page<Comment> resultPage = commentService.page(commentPage, wrapper);
            
            List<Comment> records = resultPage.getRecords();
            if (!records.isEmpty()) {
                List<String> userIds = records.stream()
                        .map(Comment::getUserId)
                        .distinct()
                        .collect(Collectors.toList());

                if (!userIds.isEmpty()) {
                    List<User> users = userService.listByIds(userIds);
                    Map<String, User> userMap = users.stream()
                            .collect(Collectors.toMap(User::getId, u -> u));

                    for (Comment c : records) {
                        User u = userMap.get(c.getUserId());
                        if (u != null) {
                            c.setAccountName(u.getAccountName());
                            // Optional: update avatar/username to latest if needed
                            c.setAvatar(u.getAvatar());
                            c.setUsername(u.getUsername());
                        }
                    }
                }
            }
            
            return Result.success(Map.of(
                    "list", resultPage.getRecords(),
                    "total", resultPage.getTotal(),
                    "current", resultPage.getCurrent(),
                    "size", resultPage.getSize(),
                    "pages", resultPage.getPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评论列表失败");
        }
    }

    /**
     * 查看评论列表（完整参数版）
     */
    @GetMapping("/admin/comments")
    public Result getCommentsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String infoType,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        try {
            Page<Comment> commentPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(Comment::getContent, keyword);
            }
            
            // 信息类型筛选
            if (infoType != null && !infoType.isEmpty()) {
                wrapper.eq(Comment::getInfoType, infoType);
            }
            
            // 用户ID或用户名筛选
            if (userId != null && !userId.isEmpty()) {
                wrapper.and(q -> q.eq(Comment::getUserId, userId)
                        .or().like(Comment::getUsername, userId));
            }
            
            // 时间范围筛选
            if (startTime != null && endTime != null) {
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                LocalDateTime startDt = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDateTime();
                LocalDateTime endDt = Instant.ofEpochMilli(endTime).atZone(zoneId).toLocalDateTime();
                wrapper.between(Comment::getCreateTime, startDt, endDt);
            }
            
            // 按创建时间降序排序
            wrapper.orderByDesc(Comment::getCreateTime);
            
            // 执行分页查询
            Page<Comment> resultPage = commentService.page(commentPage, wrapper);
            
            List<Comment> records = resultPage.getRecords();
            if (!records.isEmpty()) {
                List<String> userIds = records.stream()
                        .map(Comment::getUserId)
                        .distinct()
                        .collect(Collectors.toList());

                if (!userIds.isEmpty()) {
                    List<User> users = userService.listByIds(userIds);
                    Map<String, User> userMap = users.stream()
                            .collect(Collectors.toMap(User::getId, u -> u));

                    for (Comment c : records) {
                        User u = userMap.get(c.getUserId());
                        if (u != null) {
                            c.setAccountName(u.getAccountName());
                            // Optional: update avatar/username to latest if needed
                            c.setAvatar(u.getAvatar());
                            c.setUsername(u.getUsername());
                        }
                    }
                }
            }
            
            return Result.success(Map.of(
                    "list", resultPage.getRecords(),
                    "total", resultPage.getTotal(),
                    "current", resultPage.getCurrent(),
                    "size", resultPage.getSize(),
                    "pages", resultPage.getPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评论列表失败");
        }
    }

    /**
     * 删除违规评论
     */
    @DeleteMapping("/admin/comment/{id}")
    @Log("DELETE_COMMENT")
    public Result deleteComment(@PathVariable String id) {
        try {
            boolean deleted = commentService.removeById(id);
            if (deleted) {
                return Result.success("删除评论成功");
            } else {
                return Result.error("评论不存在或已被删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除评论失败");
        }
    }

    /**
     * 删除违规评论（复数形式路径）
     */
    @DeleteMapping("/admin/comments/{id}")
    @Log("DELETE_COMMENT")
    public Result deleteCommentByPluralPath(@PathVariable String id) {
        try {
            boolean deleted = commentService.removeById(id);
            if (deleted) {
                return Result.success("删除评论成功");
            } else {
                return Result.error("评论不存在或已被删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除评论失败");
        }
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/admin/comments/{id}")
    public Result getCommentDetail(@PathVariable String id) {
        try {
            Comment comment = commentService.getById(id);
            if (comment == null) {
                return Result.error("评论不存在");
            }
            return Result.success(comment);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评论详情失败");
        }
    }

    /**
     * 新增评论
     */
    @PostMapping("/admin/comments")
    @Log("ADD_COMMENT")
    public Result createComment(@RequestBody Comment comment) {
        try {
            comment.setId(UUID.randomUUID().toString());
            comment.setCreateTime(LocalDateTime.now());
            // 默认点赞数为0
            if (comment.getLikeCount() == null) {
                comment.setLikeCount(0);
            }
            
            boolean saved = commentService.save(comment);
            if (saved) {
                return Result.success("新增评论成功");
            } else {
                return Result.error("新增评论失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增评论失败");
        }
    }

    /**
     * 更新评论
     */
    @PutMapping("/admin/comments/{id}")
    @Log("UPDATE_COMMENT")
    public Result updateComment(@PathVariable String id, @RequestBody Comment comment) {
        try {
            Comment existing = commentService.getById(id);
            if (existing == null) {
                return Result.error("评论不存在");
            }
            
            comment.setId(id);
            // 保持原有创建时间和用户ID（如果前端没传）
            comment.setCreateTime(existing.getCreateTime());
            if (comment.getUserId() == null) {
                comment.setUserId(existing.getUserId());
            }
            
            boolean updated = commentService.updateById(comment);
            if (updated) {
                return Result.success("更新评论成功");
            } else {
                return Result.error("更新评论失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新评论失败");
        }
    }

    /**
     * 批量删除评论
     */
    @DeleteMapping("/admin/comments/batch")
    @Log("BATCH_DELETE_COMMENTS")
    public Result batchDeleteComments(@RequestBody Map<String, List<String>> params) {
        try {
            List<String> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的评论");
            }
            
            boolean deleted = commentService.removeBatchByIds(ids);
            if (deleted) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除失败");
        }
    }

    // ========== 活动记录管理接口 ==========

    /**
     * 查看活动记录列表
     */
    @GetMapping("/admin/activity/list")
    public Result getActivityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String userId) {
        try {
            Page<Activities> activityPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Activities> wrapper = new LambdaQueryWrapper<>();
            
            // 按操作类型筛选
            if (action != null && !action.isEmpty()) {
                action = action.trim().toUpperCase();
                wrapper.eq(Activities::getAction, action);
            }
            
            // 按物品类型筛选
            if (itemType != null && !itemType.isEmpty()) {
                itemType = itemType.trim().toLowerCase();
                wrapper.eq(Activities::getItemType, itemType);
            }
            
            // 按用户ID筛选
            if (userId != null && !userId.isEmpty()) {
                wrapper.eq(Activities::getUserId, userId);
            }
            
            // 按创建时间降序排序
            wrapper.orderByDesc(Activities::getCreateTime);
            
            // 执行分页查询
            Page<Activities> resultPage = activitiesService.page(activityPage, wrapper);
            
            return Result.success(Map.of(
                    "list", resultPage.getRecords(),
                    "total", resultPage.getTotal(),
                    "current", resultPage.getCurrent(),
                    "size", resultPage.getSize(),
                    "pages", resultPage.getPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取活动记录列表失败");
        }
    }

    /**
     * 获取活动记录详情
     */
    @GetMapping("/admin/activities/{id}")
    public Result getActivityDetail(@PathVariable String id) {
        try {
            Activities activity = activitiesService.getById(id);
            if (activity == null) {
                return Result.error("活动记录不存在");
            }
            return Result.success(activity);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取活动记录详情失败");
        }
    }

    /**
     * 新增活动记录
     */
    @PostMapping("/admin/activities")
    public Result createActivity(@RequestBody Activities activity) {
        try {
            activity.setId(UUID.randomUUID().toString());
            activity.setCreateTime(LocalDateTime.now());
            
            boolean saved = activitiesService.save(activity);
            if (saved) {
                return Result.success("新增活动记录成功");
            } else {
                return Result.error("新增活动记录失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增活动记录失败");
        }
    }

    /**
     * 更新活动记录
     */
    @PutMapping("/admin/activities/{id}")
    public Result updateActivity(@PathVariable String id, @RequestBody Activities activity) {
        try {
            Activities existing = activitiesService.getById(id);
            if (existing == null) {
                return Result.error("活动记录不存在");
            }
            
            activity.setId(id);
            // 保持原有创建时间
            activity.setCreateTime(existing.getCreateTime());
            
            boolean updated = activitiesService.updateById(activity);
            if (updated) {
                return Result.success("更新活动记录成功");
            } else {
                return Result.error("更新活动记录失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新活动记录失败");
        }
    }

    /**
     * 删除活动记录
     */
    @DeleteMapping("/admin/activities/{id}")
    public Result deleteActivity(@PathVariable String id) {
        try {
            boolean deleted = activitiesService.removeById(id);
            if (deleted) {
                return Result.success("删除活动记录成功");
            } else {
                return Result.error("活动记录不存在或已被删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除活动记录失败");
        }
    }

    /**
     * 批量删除活动记录
     */
    @DeleteMapping("/admin/activities/batch")
    public Result batchDeleteActivities(@RequestBody Map<String, List<String>> params) {
        try {
            List<String> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的记录");
            }
            
            boolean deleted = activitiesService.removeBatchByIds(ids);
            if (deleted) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除失败");
        }
    }

    /**
     * 查看活动记录列表（完整参数版）
     */
    @GetMapping("/admin/activities")
    public Result getActivitiesList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        try {
            Page<Activities> activityPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Activities> wrapper = new LambdaQueryWrapper<>();
            
            // 用户ID筛选
            if (userId != null && !userId.isEmpty()) {
                wrapper.eq(Activities::getUserId, userId);
            }
            
            // 按操作类型筛选
            if (action != null && !action.isEmpty()) {
                action = action.trim().toUpperCase();
                wrapper.eq(Activities::getAction, action);
            }
            
            // 按物品类型筛选
            if (itemType != null && !itemType.isEmpty()) {
                itemType = itemType.trim().toLowerCase();
                wrapper.eq(Activities::getItemType, itemType);
            }
            
            // 时间范围筛选
            if (startTime != null && endTime != null) {
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                LocalDateTime startDt = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDateTime();
                LocalDateTime endDt = Instant.ofEpochMilli(endTime).atZone(zoneId).toLocalDateTime();
                wrapper.between(Activities::getCreateTime, startDt, endDt);
            }
            
            // 按创建时间降序排序
            wrapper.orderByDesc(Activities::getCreateTime);
            
            // 执行分页查询
            Page<Activities> resultPage = activitiesService.page(activityPage, wrapper);
            
            return Result.success(Map.of(
                    "list", resultPage.getRecords(),
                    "total", resultPage.getTotal(),
                    "current", resultPage.getCurrent(),
                    "size", resultPage.getSize(),
                    "pages", resultPage.getPages()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取活动记录列表失败");
        }
    }


    // Duplicates removed


    /**
     * 更新用户角色（通过路径参数）
     */
    @PutMapping("/admin/users/{id}/role")
    public Result updateUserRoleByPath(@PathVariable String id, @RequestBody Map<String, String> params) {
        try {
            String role = params.get("role");
            
            if (role == null) {
                return Result.error("角色参数不能为空");
            }
            
            // 验证角色是否合法
            if (!Arrays.asList("STUDENT", "TEACHER", "ADMIN").contains(role)) {
                return Result.error("角色类型不合法");
            }
            
            // 更新用户角色
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            user.setRole(role);
            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);
            
            return Result.success("角色更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("角色更新失败");
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/admin/users/{id}/status")
    public Result updateUserStatus(@PathVariable String id, @RequestBody Map<String, Object> params) {
        try {
            Object statusObj = params.get("status");
            if (statusObj == null) {
                return Result.error("状态不能为空");
            }

            Integer statusInt = null;
            if (statusObj instanceof Number) {
                statusInt = ((Number) statusObj).intValue();
            } else if (statusObj instanceof String) {
                String s = (String) statusObj;
                if ("NORMAL".equalsIgnoreCase(s) || "0".equals(s)) {
                    statusInt = 0;
                } else if ("BANNED".equalsIgnoreCase(s) || "1".equals(s)) {
                    statusInt = 1;
                } else {
                     try {
                        statusInt = Integer.parseInt(s);
                     } catch (NumberFormatException e) {
                        // ignore
                     }
                }
            }
            
            if (statusInt == null || (statusInt != 0 && statusInt != 1)) {
                 return Result.error("状态类型不合法 (应为 0:正常 或 1:封禁)");
            }

            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 管理员不可被封禁
            if (statusInt == 1 && "ADMIN".equalsIgnoreCase(user.getRole())) {
                return Result.error("管理员不可被封禁");
            }
            
            user.setStatus(statusInt);
            boolean updated = userService.updateById(user);
            if (updated) {
                return Result.success("用户状态更新成功");
            } else {
                return Result.error("用户状态更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新用户状态失败");
        }
    }

    /**
     * 新增失物信息
     */
    @PostMapping("/admin/lost")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result createLostInfo(@RequestBody LostInfo lostInfo) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            String userId = map != null && map.get("id") != null ? map.get("id").toString() : null;
            
            // 如果前端没有传ID，生成一个
            if (lostInfo.getId() == null) {
                lostInfo.setId(UUID.randomUUID().toString());
            }
            
            // 如果前端没有传UserId，使用当前登录管理员ID
            if (lostInfo.getUserId() == null) {
                lostInfo.setUserId(userId);
            }
            
            // 设置默认值
            if (lostInfo.getStatus() == null) {
                lostInfo.setStatus("APPROVED"); // 管理员添加默认通过
            }
            if (lostInfo.getViewCount() == null) {
                lostInfo.setViewCount(0);
            }
            if (lostInfo.getPublishTime() == null) {
                lostInfo.setPublishTime(LocalDateTime.now());
            }
            
            boolean saved = lostInfoService.save(lostInfo);
            if (saved) {
                // 记录日志
                Activities act = new Activities();
                act.setId(UUID.randomUUID().toString());
                act.setUserId(userId);
                act.setAction("PUBLISH");
                act.setItemType("lost");
                act.setItemId(lostInfo.getId());
                act.setContent("管理员发布了失物信息：" + lostInfo.getName());
                act.setCreateTime(LocalDateTime.now());
                activitiesService.save(act);
                
                return Result.success("新增失物信息成功");
            } else {
                return Result.error("新增失物信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增失物信息失败");
        }
    }

    /**
     * 更新失物信息
     */
    @PutMapping("/admin/lost/{id}")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result updateLostInfo(@PathVariable String id, @RequestBody LostInfo lostInfo) {
        try {
            LostInfo existing = lostInfoService.getById(id);
            if (existing == null) {
                return Result.error("失物信息不存在");
            }
            
            lostInfo.setId(id);
            // 保持一些原有字段
            if (lostInfo.getUserId() == null) lostInfo.setUserId(existing.getUserId());
            if (lostInfo.getPublishTime() == null) lostInfo.setPublishTime(existing.getPublishTime());
            if (lostInfo.getViewCount() == null) lostInfo.setViewCount(existing.getViewCount());
            
            boolean updated = lostInfoService.updateById(lostInfo);
            if (updated) {
                return Result.success("更新失物信息成功");
            } else {
                return Result.error("更新失物信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失物信息失败");
        }
    }

    /**
     * 更新失物信息状态
     */
    @PutMapping("/admin/lost/{id}/status")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result updateLostInfoStatus(@PathVariable String id, @RequestBody Map<String, String> params) {
        try {
            String status = params.get("status");
            if (status != null) {
                status = status.toUpperCase();
            }
            
            if (status == null) {
                return Result.error("状态参数不能为空");
            }
            
            // 验证状态是否合法
            if (!Arrays.asList("PENDING", "APPROVED", "REJECTED", "SOLVED").contains(status)) {
                return Result.error("状态类型不合法");
            }
            
            // 更新失物状态
            LostInfo lostInfo = lostInfoService.getById(id);
            if (lostInfo == null) {
                return Result.error("失物信息不存在");
            }
            
            lostInfo.setStatus(status);
            lostInfoService.updateById(lostInfo);
            
            return Result.success("失物状态更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("失物状态更新失败");
        }
    }

    /**
     * 新增招领信息
     */
    @PostMapping("/admin/find")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result createFindInfo(@RequestBody com.lzy.lostandfound.entity.FindInfo findInfo) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            String userId = map != null && map.get("id") != null ? map.get("id").toString() : null;
            
            // 如果前端没有传ID，生成一个
            if (findInfo.getId() == null) {
                findInfo.setId(UUID.randomUUID().toString());
            }
            
            // 如果前端没有传UserId，使用当前登录管理员ID
            if (findInfo.getUserId() == null) {
                findInfo.setUserId(userId);
            }
            
            // 设置默认值
            if (findInfo.getStatus() == null) {
                findInfo.setStatus("APPROVED"); // 管理员添加默认通过
            }
            if (findInfo.getViewCount() == null) {
                findInfo.setViewCount(0);
            }
            if (findInfo.getPublishTime() == null) {
                findInfo.setPublishTime(LocalDateTime.now());
            }
            
            boolean saved = findInfoService.save(findInfo);
            if (saved) {
                // 记录日志
                Activities act = new Activities();
                act.setId(UUID.randomUUID().toString());
                act.setUserId(userId);
                act.setAction("PUBLISH");
                act.setItemType("find");
                act.setItemId(findInfo.getId());
                act.setContent("管理员发布了招领信息：" + findInfo.getName());
                act.setCreateTime(LocalDateTime.now());
                activitiesService.save(act);
                
                return Result.success("新增招领信息成功");
            } else {
                return Result.error("新增招领信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增招领信息失败");
        }
    }

    /**
     * 更新招领信息
     */
    @PutMapping("/admin/find/{id}")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result updateFindInfo(@PathVariable String id, @RequestBody com.lzy.lostandfound.entity.FindInfo findInfo) {
        try {
            com.lzy.lostandfound.entity.FindInfo existing = findInfoService.getById(id);
            if (existing == null) {
                return Result.error("招领信息不存在");
            }
            
            findInfo.setId(id);
            // 保持一些原有字段
            if (findInfo.getUserId() == null) findInfo.setUserId(existing.getUserId());
            if (findInfo.getPublishTime() == null) findInfo.setPublishTime(existing.getPublishTime());
            if (findInfo.getViewCount() == null) findInfo.setViewCount(existing.getViewCount());
            
            boolean updated = findInfoService.updateById(findInfo);
            if (updated) {
                return Result.success("更新招领信息成功");
            } else {
                return Result.error("更新招领信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新招领信息失败");
        }
    }

    /**
     * 更新招领信息状态
     */
    @PutMapping("/admin/find/{id}/status")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result updateFindInfoStatus(@PathVariable String id, @RequestBody Map<String, String> params) {
        try {
            String status = params.get("status");
            if (status != null) {
                status = status.toUpperCase();
            }
            
            if (status == null) {
                return Result.error("状态参数不能为空");
            }
            
            // 验证状态是否合法
            if (!Arrays.asList("PENDING", "APPROVED", "REJECTED", "SOLVED").contains(status)) {
                return Result.error("状态类型不合法");
            }
            
            // 更新招领状态
            com.lzy.lostandfound.entity.FindInfo findInfo = findInfoService.getById(id);
            if (findInfo == null) {
                return Result.error("招领信息不存在");
            }
            
            findInfo.setStatus(status);
            findInfoService.updateById(findInfo);
            
            return Result.success("招领状态更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("招领状态更新失败");
        }
    }

    /**
     * 删除失物信息
     */
    @DeleteMapping("/admin/lost/{id}")
    @Log("DELETE")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result deleteLostInfo(@PathVariable String id) {
        try {
            // 检查失物信息是否存在
            LostInfo lostInfo = lostInfoService.getById(id);
            if (lostInfo == null) {
                return Result.error("失物信息不存在");
            }
            
            // 删除失物信息
            boolean deleted = lostInfoService.removeById(id);
            if (deleted) {
                return Result.success("失物信息删除成功");
            } else {
                return Result.error("失物信息删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失物信息失败");
        }
    }

    /**
     * 批量删除失物信息
     */
    @DeleteMapping("/admin/lost/batch")
    @Log("DELETE")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result batchDeleteLostInfo(@RequestBody Map<String, List<String>> params) {
        try {
            List<String> ids = params != null ? params.get("ids") : null;
            if (ids == null || ids.isEmpty()) {
                return Result.error("未选择要删除的失物信息");
            }
            boolean deleted = lostInfoService.removeBatchByIds(ids);
            if (deleted) {
                return Result.success("批量删除失物信息成功");
            } else {
                return Result.error("批量删除失物信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除失物信息失败");
        }
    }

    /**
     * 删除招领信息
     */
    @DeleteMapping("/admin/find/{id}")
    @Log("DELETE")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result deleteFindInfo(@PathVariable String id) {
        try {
            // 检查招领信息是否存在
            com.lzy.lostandfound.entity.FindInfo findInfo = findInfoService.getById(id);
            if (findInfo == null) {
                return Result.error("招领信息不存在");
            }
            
            // 删除招领信息
            boolean deleted = findInfoService.removeById(id);
            if (deleted) {
                return Result.success("招领信息删除成功");
            } else {
                return Result.error("招领信息删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除招领信息失败");
        }
    }

    /**
     * 批量删除招领信息
     */
    @DeleteMapping("/admin/find/batch")
    @Log("DELETE")
    @CacheEvict(cacheNames = {"LostListCache","FindListCache"}, allEntries = true)
    public Result batchDeleteFindInfo(@RequestBody Map<String, List<String>> params) {
        try {
            List<String> ids = params != null ? params.get("ids") : null;
            if (ids == null || ids.isEmpty()) {
                return Result.error("未选择要删除的招领信息");
            }
            boolean deleted = findInfoService.removeBatchByIds(ids);
            if (deleted) {
                return Result.success("批量删除招领信息成功");
            } else {
                return Result.error("批量删除招领信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除招领信息失败");
        }
    }

    // ================== 白名单管理 ==================

    /**
     * 获取白名单列表
     */
    @GetMapping("/admin/whitelist")
    public Result getWhitelist(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer pageSize,
            @RequestParam(required = false) String query) {
        try {
            Page<StudentWhitelist> p = new Page<>(page, pageSize);
            LambdaQueryWrapper<StudentWhitelist> wrapper = new LambdaQueryWrapper<>();
            if (query != null && !query.isEmpty()) {
                wrapper.like(StudentWhitelist::getStudentId, query)
                        .or()
                        .like(StudentWhitelist::getName, query);
            }
            wrapper.orderByDesc(StudentWhitelist::getId);
            Page<StudentWhitelist> result = studentWhitelistService.page(p, wrapper);
            Map<String, Object> map = new HashMap<>();
            map.put("list", result.getRecords());
            map.put("total", result.getTotal());
            return Result.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取白名单失败");
        }
    }

    /**
     * 添加白名单
     */
    @PostMapping("/admin/whitelist")
    public Result addWhitelist(@RequestBody StudentWhitelist whitelist) {
        try {
            if (whitelist.getStudentId() == null || whitelist.getName() == null) {
                return Result.error("学号和姓名不能为空");
            }
            // 查重
            long count = studentWhitelistService.count(new LambdaQueryWrapper<StudentWhitelist>()
                    .eq(StudentWhitelist::getStudentId, whitelist.getStudentId()));
            if (count > 0) {
                return Result.error("该学号已存在");
            }
            studentWhitelistService.save(whitelist);
            return Result.success("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败");
        }
    }

    /**
     * 更新白名单
     */
    @PutMapping("/admin/whitelist/{id}")
    public Result updateWhitelist(@PathVariable Long id, @RequestBody StudentWhitelist whitelist) {
        try {
            StudentWhitelist existing = studentWhitelistService.getById(id);
            if (existing == null) {
                return Result.error("白名单记录不存在");
            }
            
            // 如果修改了学号，需要查重
            if (whitelist.getStudentId() != null && !whitelist.getStudentId().equals(existing.getStudentId())) {
                long count = studentWhitelistService.count(new LambdaQueryWrapper<StudentWhitelist>()
                        .eq(StudentWhitelist::getStudentId, whitelist.getStudentId()));
                if (count > 0) {
                    return Result.error("该学号已存在");
                }
            }
            
            whitelist.setId(id);
            boolean updated = studentWhitelistService.updateById(whitelist);
            if (updated) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败");
        }
    }

    /**
     * 下载白名单导入模板
     */
    @GetMapping("/admin/whitelist/template")
    public void downloadWhitelistTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("白名单导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=utf-8''" + fileName + ".xlsx");
        
        // 创建示例数据
        List<WhitelistImportDTO> list = new ArrayList<>();
        WhitelistImportDTO example = new WhitelistImportDTO();
        example.setStudentId("20210001");
        example.setName("张三");
        example.setCollege("计算机学院");
        list.add(example);
        
        EasyExcel.write(response.getOutputStream(), WhitelistImportDTO.class)
                .sheet("模板")
                .doWrite(list);
    }

    /**
     * 导入白名单 (支持 Excel)
     */
    @PostMapping("/admin/whitelist/import")
    @Log("IMPORT_WHITELIST")
    public Result importWhitelist(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件为空");
            }
            
            // 读取 Excel
            List<WhitelistImportDTO> list = EasyExcel.read(file.getInputStream())
                    .head(WhitelistImportDTO.class)
                    .sheet()
                    .doReadSync();
            
            if (list == null || list.isEmpty()) {
                return Result.error("文件内容为空");
            }
            
            int successCount = 0;
            List<StudentWhitelist> saveList = new ArrayList<>();
            
            for (WhitelistImportDTO dto : list) {
                if (dto.getStudentId() == null || dto.getName() == null) {
                    continue;
                }
                
                // 查重
                long count = studentWhitelistService.count(new LambdaQueryWrapper<StudentWhitelist>()
                        .eq(StudentWhitelist::getStudentId, dto.getStudentId()));
                        
                if (count == 0) {
                    StudentWhitelist sw = new StudentWhitelist();
                    sw.setStudentId(dto.getStudentId());
                    sw.setName(dto.getName());
                    sw.setCollege(dto.getCollege());
                    sw.setCreateTime(LocalDateTime.now());
                    sw.setUpdateTime(LocalDateTime.now());
                    sw.setDeleted(0);
                    saveList.add(sw);
                    successCount++;
                }
            }
            
            if (!saveList.isEmpty()) {
                studentWhitelistService.saveBatch(saveList);
            }
            
            return Result.success("导入成功，共导入 " + successCount + " 条数据");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 删除白名单
     */
    @DeleteMapping("/admin/whitelist/{id}")
    public Result deleteWhitelist(@PathVariable Long id) {
        try {
            studentWhitelistService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    /**
     * 批量删除白名单
     */
    @DeleteMapping("/admin/whitelist/batch")
    public Result batchDeleteWhitelist(@RequestBody Map<String, List<Long>> params) {
        try {
            List<Long> ids = params != null ? params.get("ids") : null;
            if (ids == null || ids.isEmpty()) {
                return Result.error("未选择数据");
            }
            studentWhitelistService.removeBatchByIds(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除失败");
        }
    }







}
