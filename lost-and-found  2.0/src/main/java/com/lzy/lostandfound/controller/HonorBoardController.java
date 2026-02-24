package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzy.lostandfound.entity.HonorPeriod;
import com.lzy.lostandfound.entity.HonorPeriodItem;
import com.lzy.lostandfound.service.IHonorPeriodItemService;
import com.lzy.lostandfound.service.IHonorPeriodService;
import com.lzy.lostandfound.vo.HonorBoardItemVO;
import com.lzy.lostandfound.vo.HonorBoardVO;
import com.lzy.lostandfound.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/honor")
public class HonorBoardController {

    @Autowired
    private IHonorPeriodService honorPeriodService;

    @Autowired
    private IHonorPeriodItemService honorPeriodItemService;

    @GetMapping("/board/currentWeek")
    public Result currentWeekBoard() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LambdaQueryWrapper<HonorPeriod> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HonorPeriod::getPeriodType, "WEEKLY");
            wrapper.le(HonorPeriod::getPeriodStart, now);
            wrapper.ge(HonorPeriod::getPeriodEnd, now);
            wrapper.orderByDesc(HonorPeriod::getPeriodStart);
            wrapper.last("limit 1");
            HonorPeriod period = honorPeriodService.getOne(wrapper, false);
            if (period == null) {
                LambdaQueryWrapper<HonorPeriod> latestWrapper = new LambdaQueryWrapper<>();
                latestWrapper.eq(HonorPeriod::getPeriodType, "WEEKLY");
                latestWrapper.orderByDesc(HonorPeriod::getPeriodStart);
                latestWrapper.last("limit 1");
                period = honorPeriodService.getOne(latestWrapper, false);
            }
            if (period == null) {
                HonorBoardVO empty = new HonorBoardVO();
                empty.setPeriodId(null);
                empty.setPeriodType("WEEKLY");
                empty.setPeriodStart(null);
                empty.setPeriodEnd(null);
                empty.setTopN(0);
                empty.setTotalCompletedCount(0);
                empty.setStatus("PENDING");
                empty.setList(List.of());
                return Result.success(empty);
            }
            HonorBoardVO vo = buildBoardVO(period, period.getTopN());
            return Result.success(vo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取当前光荣榜失败");
        }
    }

    @GetMapping("/board/weekly")
    public Result weeklyBoard(@RequestParam Long startTime,
                              @RequestParam Long endTime,
                              @RequestParam(required = false) Integer topN) {
        try {
            ZoneId zoneId = ZoneId.of("Asia/Shanghai");
            LocalDateTime start = Instant.ofEpochMilli(startTime).atZone(zoneId).toLocalDateTime();
            LocalDateTime end = Instant.ofEpochMilli(endTime).atZone(zoneId).toLocalDateTime();
            LambdaQueryWrapper<HonorPeriod> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HonorPeriod::getPeriodType, "WEEKLY");
            wrapper.eq(HonorPeriod::getPeriodStart, start);
            wrapper.eq(HonorPeriod::getPeriodEnd, end);
            wrapper.last("limit 1");
            HonorPeriod period = honorPeriodService.getOne(wrapper, false);
            if (period == null) {
                return Result.success(null);
            }
            int limit = Objects.requireNonNullElse(topN, period.getTopN());
            HonorBoardVO vo = buildBoardVO(period, limit);
            return Result.success(vo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取历史光荣榜失败");
        }
    }

    private HonorBoardVO buildBoardVO(HonorPeriod period, int topN) {
        LambdaQueryWrapper<HonorPeriodItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(HonorPeriodItem::getPeriodId, period.getId());
        itemWrapper.orderByAsc(HonorPeriodItem::getRank);
        if (topN > 0) {
            itemWrapper.last("limit " + topN);
        }
        List<HonorPeriodItem> items = honorPeriodItemService.list(itemWrapper);
        List<HonorBoardItemVO> list = items.stream().map(this::toItemVO).collect(Collectors.toList());
        HonorBoardVO vo = new HonorBoardVO();
        vo.setPeriodId(period.getId());
        vo.setPeriodType(period.getPeriodType());
        vo.setPeriodStart(period.getPeriodStart());
        vo.setPeriodEnd(period.getPeriodEnd());
        vo.setTopN(period.getTopN());
        vo.setTotalCompletedCount(period.getTotalCompletedCount());
        vo.setStatus(period.getStatus());
        vo.setList(list);
        return vo;
    }

    private HonorBoardItemVO toItemVO(HonorPeriodItem item) {
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
        return vo;
    }
}
