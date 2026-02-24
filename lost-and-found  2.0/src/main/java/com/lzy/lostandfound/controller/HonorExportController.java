package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzy.lostandfound.entity.HonorPeriod;
import com.lzy.lostandfound.entity.HonorPeriodItem;
import com.lzy.lostandfound.service.IHonorPeriodItemService;
import com.lzy.lostandfound.service.IHonorPeriodService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class HonorExportController {

    @Autowired
    private IHonorPeriodService honorPeriodService;

    @Autowired
    private IHonorPeriodItemService honorPeriodItemService;

    @GetMapping("/admin/honor/export")
    public void export(@RequestParam String periodId,
                       @RequestParam(required = false, defaultValue = "xlsx") String format,
                       HttpServletResponse response) {
        try {
            HonorPeriod period = honorPeriodService.getById(periodId);
            if (period == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            LambdaQueryWrapper<HonorPeriodItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HonorPeriodItem::getPeriodId, periodId);
            wrapper.orderByAsc(HonorPeriodItem::getRank);
            List<HonorPeriodItem> items = honorPeriodItemService.list(wrapper);
            if (!"csv".equalsIgnoreCase(format)) {
                format = "xlsx";
            }
            String fileName = "honor_board_" + period.getId() + ("csv".equalsIgnoreCase(format) ? ".csv" : ".xlsx");
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            if ("csv".equalsIgnoreCase(format)) {
                response.setContentType("text/csv;charset=UTF-8");
            } else {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
            }
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedName);
            StringBuilder sb = new StringBuilder();
            sb.append("rank,userId,username,name,className,departmentName,avatar,completedCount,points,lastCompletedAt\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (HonorPeriodItem item : items) {
                sb.append(item.getRank() == null ? "" : item.getRank()).append(',');
                sb.append(item.getUserId() == null ? "" : item.getUserId()).append(',');
                sb.append(item.getUsername() == null ? "" : item.getUsername()).append(',');
                sb.append(item.getName() == null ? "" : item.getName()).append(',');
                sb.append(item.getClassName() == null ? "" : item.getClassName()).append(',');
                sb.append(item.getDepartmentName() == null ? "" : item.getDepartmentName()).append(',');
                sb.append(item.getAvatar() == null ? "" : item.getAvatar()).append(',');
                sb.append(item.getCompletedCount() == null ? "" : item.getCompletedCount()).append(',');
                sb.append(item.getPoints() == null ? "" : item.getPoints()).append(',');
                if (item.getLastCompletedAt() != null) {
                    sb.append(item.getLastCompletedAt().format(formatter));
                }
                sb.append("\n");
            }
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ignored) {
            }
        }
    }
}

