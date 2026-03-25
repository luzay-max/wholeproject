package com.lzy.lostandfound.task;

import com.lzy.lostandfound.service.HonorBoardGenerateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 光荣榜定时任务
 * 每周一凌晨 1:00 自动生成上周的光荣榜
 */
@Component
public class HonorBoardScheduleTask {

    private static final Logger logger = LoggerFactory.getLogger(HonorBoardScheduleTask.class);

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private HonorBoardGenerateService honorBoardGenerateService;

    /**
     * 每周一凌晨 1:00 执行
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 ? * MON")
    public void generateWeeklyHonorBoard() {
        String startTime = LocalDateTime.now().format(formatter);
        logger.info("========== 开始执行光荣榜定时任务 ==========");
        logger.info("开始时间: {}", startTime);

        try {
            HonorBoardGenerateService.HonorBoard result = honorBoardGenerateService.generateLastWeekHonorBoard();

            logger.info("光荣榜生成成功!");
            logger.info("周期ID: {}", result.getPeriodId());
            logger.info("统计周期: {} ~ {}", result.getPeriodStart(), result.getPeriodEnd());
            logger.info("参与用户数: {}", result.getTotalUsers());
            logger.info("上榜人数: {}", result.getTotalItems());
            logger.info("========== 光荣榜定时任务执行完成 ==========");

        } catch (Exception e) {
            logger.error("生成光荣榜失败!", e);
            logger.error("========== 光荣榜定时任务执行失败 ==========");
        }
    }
}

