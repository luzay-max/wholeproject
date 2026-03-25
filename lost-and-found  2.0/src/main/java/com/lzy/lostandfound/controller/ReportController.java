package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.dto.ItemReportRequest;
import com.lzy.lostandfound.service.IReportService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private IReportService reportService;

    @PostMapping("/item")
    @Log("REPORT_ITEM")
    public Result reportItem(@Valid @RequestBody ItemReportRequest request) {
        Map<String, Object> userMap = ThreadLocalUtil.get();
        String reporterId = String.valueOf(userMap.get("id"));
        String reporterName = String.valueOf(userMap.getOrDefault("username", "用户"));
        return reportService.reportItem(reporterId, reporterName, request);
    }
}
