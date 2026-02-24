package com.lzy.lostandfound.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.entity.HonorPeriod;
import com.lzy.lostandfound.entity.HonorPeriodItem;
import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.mapper.FindInfoMapper;
import com.lzy.lostandfound.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class HonorBoardGenerateService extends ServiceImpl<FindInfoMapper, FindInfo> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IHonorPeriodService honorPeriodService;

    @Autowired
    private IHonorPeriodItemService honorPeriodItemService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 生成光荣榜
     * @param type 0=本周, 1=上周, 2=自定义日期范围
     * @param startDate 自定义起始日期 (type=2时必填，格式: yyyy-MM-dd)
     * @param endDate 自定义结束日期 (type=2时必填，格式: yyyy-MM-dd)
     */
    public HonorBoard generateHonorBoard(int type, String startDate, String endDate) {
        // 1. 计算时间范围
        LocalDate today = LocalDate.now();
        LocalDate weekStart, weekEnd;

        if (type == 0) {
            // 本周（从本周一到当前时间）
            weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            weekEnd = today;
        } else if (type == 1) {
            // 上周
            weekStart = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).minusWeeks(1);
            weekEnd = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).minusWeeks(1);
        } else {
            // 自定义日期范围
            weekStart = LocalDate.parse(startDate);
            weekEnd = LocalDate.parse(endDate);
        }

        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(LocalTime.MAX);

        String typeLabel = type == 0 ? "本周" : (type == 1 ? "上周" : "自定义(" + startDate + "~" + endDate + ")");
        System.out.println("=== 生成" + typeLabel + "光荣榜 ===");
        System.out.println("统计周期: " + startDateTime + " ~ " + endDateTime);

        // 2. 统计每个用户完成的招领数量
        String sql = """
            SELECT
                fi.user_id,
                COUNT(*) as completed_count,
                MAX(fi.update_time) as last_completed_at
            FROM find_info fi
            WHERE fi.status = 'SOLVED'
            AND fi.update_time >= ? AND fi.update_time < ?
            GROUP BY fi.user_id
            ORDER BY completed_count DESC, last_completed_at ASC
            """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql,
            startDateTime, endDateTime);

        System.out.println("找到 " + results.size() + " 位用户有完成的招领记录");

        // 3. 查询所有用户信息（用于获取用户名、姓名、头像等）
        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, User> userMap = new HashMap<>();
        for (User user : allUsers) {
            userMap.put(user.getId(), user);
        }

        // 4. 创建周期记录
        String periodId = UUID.randomUUID().toString().replace("-", "");
        HonorPeriod period = new HonorPeriod();
        period.setId(periodId);
        period.setPeriodType("WEEKLY");
        period.setPeriodStart(startDateTime);
        period.setPeriodEnd(endDateTime);
        period.setTopN(10); // 默认显示前10名
        period.setTotalCompletedCount(results.size());
        period.setStatus("PENDING");
        period.setCreatedAt(LocalDateTime.now());
        period.setUpdatedAt(LocalDateTime.now());

        honorPeriodService.save(period);
        System.out.println("创建周期记录成功: " + periodId);

        // 5. 创建周期明细记录
        int rank = 1;
        for (Map<String, Object> row : results) {
            String userId = (String) row.get("user_id");
            Integer completedCount = ((Number) row.get("completed_count")).intValue();
            Object lastCompletedAtObj = row.get("last_completed_at");
            LocalDateTime lastCompletedAt;
            
            // 处理不同类型的时间值
            if (lastCompletedAtObj instanceof Timestamp) {
                lastCompletedAt = ((Timestamp) lastCompletedAtObj).toLocalDateTime();
            } else if (lastCompletedAtObj instanceof LocalDateTime) {
                lastCompletedAt = (LocalDateTime) lastCompletedAtObj;
            } else {
                lastCompletedAt = LocalDateTime.now();
            }

            User user = userMap.get(userId);

            HonorPeriodItem item = new HonorPeriodItem();
            item.setId(UUID.randomUUID().toString().replace("-", ""));
            item.setPeriodId(periodId);
            item.setUserId(userId);
            item.setRank(rank++);
            item.setCompletedCount(completedCount);
            item.setPoints(completedCount * 10); // 每完成一次招领获得10积分
            item.setLastCompletedAt(lastCompletedAt);

            if (user != null) {
                item.setUsername(user.getUsername());
                item.setName(user.getName());
                item.setAvatar(user.getAvatar());
                // 填充学院信息
                item.setDepartmentName(user.getCollege());
            }

            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());

            honorPeriodItemService.save(item);
        }

        System.out.println("创建 " + (rank - 1) + " 条明细记录");

        // 返回生成结果
        HonorBoard result = new HonorBoard();
        result.setPeriodId(periodId);
        result.setPeriodStart(startDateTime);
        result.setPeriodEnd(endDateTime);
        result.setTotalUsers(results.size());
        result.setTotalItems(rank - 1);
        result.setType(type);
        result.setTypeLabel(typeLabel);
        return result;
    }

    /**
     * 生成上周光荣榜（定时任务调用）
     */
    public HonorBoard generateLastWeekHonorBoard() {
        return generateHonorBoard(1, null, null);
    }

    /**
     * 生成结果封装类
     */
    public static class HonorBoard {
        private String periodId;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private int totalUsers;
        private int totalItems;
        private int type;
        private String typeLabel;

        // getters and setters
        public String getPeriodId() { return periodId; }
        public void setPeriodId(String periodId) { this.periodId = periodId; }
        public LocalDateTime getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
        public LocalDateTime getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        public int getType() { return type; }
        public void setType(int type) { this.type = type; }
        public String getTypeLabel() { return typeLabel; }
        public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }
    }
}
