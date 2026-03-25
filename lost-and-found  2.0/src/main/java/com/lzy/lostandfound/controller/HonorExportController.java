package com.lzy.lostandfound.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzy.lostandfound.dto.HonorExportDTO;
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
import java.util.stream.Collectors;

@RestController
public class HonorExportController {
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            if ("csv".equalsIgnoreCase(format)) {
                response.setContentType("text/csv;charset=UTF-8");
            } else {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
            }
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);

            List<HonorExportDTO> exportRows = items.stream()
                    .map(this::toExportRow)
                    .collect(Collectors.toList());

            if ("csv".equalsIgnoreCase(format)) {
                writeCsv(response, exportRows);
            } else {
                EasyExcel.write(response.getOutputStream(), HonorExportDTO.class)
                        .sheet("光荣榜")
                        .doWrite(exportRows);
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ignored) {
            }
        }
    }

    private HonorExportDTO toExportRow(HonorPeriodItem item) {
        return new HonorExportDTO(
                item.getRank(),
                item.getUserId(),
                item.getUsername(),
                item.getName(),
                item.getClassName(),
                item.getDepartmentName(),
                item.getAvatar(),
                item.getCompletedCount(),
                item.getPoints(),
                item.getLastCompletedAt() == null ? "" : item.getLastCompletedAt().format(EXPORT_TIME_FORMATTER)
        );
    }

    private void writeCsv(HttpServletResponse response, List<HonorExportDTO> rows) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("rank,userId,username,name,className,departmentName,avatar,completedCount,points,lastCompletedAt\n");
        for (HonorExportDTO row : rows) {
            sb.append(csvValue(row.getRank())).append(',');
            sb.append(csvValue(row.getUserId())).append(',');
            sb.append(csvValue(row.getUsername())).append(',');
            sb.append(csvValue(row.getName())).append(',');
            sb.append(csvValue(row.getClassName())).append(',');
            sb.append(csvValue(row.getDepartmentName())).append(',');
            sb.append(csvValue(row.getAvatar())).append(',');
            sb.append(csvValue(row.getCompletedCount())).append(',');
            sb.append(csvValue(row.getPoints())).append(',');
            sb.append(csvValue(row.getLastCompletedAt())).append('\n');
        }
        response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        boolean needsQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        if (!needsQuote) {
            return text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
