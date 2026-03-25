package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.service.HonorBoardGenerateService;
import com.lzy.lostandfound.vo.Result;
import com.lzy.lostandfound.anno.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 光荣榜管理接口
 * 提供手动触发生成光荣榜的功能
 */
@RestController
@RequestMapping("/admin/honor")
public class HonorManageController {

    @Autowired
    private HonorBoardGenerateService honorBoardGenerateService;

    /**
     * 手动触发生成光荣榜（管理端）
     * POST /admin/honor/generate
     * 
     * 请求体参数:
     * - type: 0=本周, 1=上周, 2=自定义日期范围 (默认1)
     * - startDate: 自定义起始日期 (type=2时必填，格式: yyyy-MM-dd)
     * - endDate: 自定义结束日期 (type=2时必填，格式: yyyy-MM-dd)
     */
    @PostMapping("/generate")
    @Log("GENERATE_HONOR_BOARD")
    public Result generateHonorBoard(@RequestBody(required = false) Map<String, Object> body) {
        try {
            // 默认生成上周
            int type = 1;
            String startDate = null;
            String endDate = null;
            
            if (body != null) {
                if (body.containsKey("type")) {
                    Object typeObj = body.get("type");
                    if (typeObj instanceof Integer) {
                        type = (Integer) typeObj;
                    } else if (typeObj instanceof String) {
                        type = Integer.parseInt((String) typeObj);
                    }
                }
                if (body.containsKey("startDate")) {
                    startDate = (String) body.get("startDate");
                }
                if (body.containsKey("endDate")) {
                    endDate = (String) body.get("endDate");
                }
            }

            // 验证参数
            if (type == 2 && (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty())) {
                return Result.error("自定义日期范围时，startDate 和 endDate 为必填参数");
            }

            HonorBoardGenerateService.HonorBoard result = honorBoardGenerateService.generateHonorBoard(type, startDate, endDate);

            Map<String, Object> data = new HashMap<>();
            data.put("periodId", result.getPeriodId());
            data.put("periodStart", result.getPeriodStart());
            data.put("periodEnd", result.getPeriodEnd());
            data.put("totalUsers", result.getTotalUsers());
            data.put("totalItems", result.getTotalItems());
            data.put("weekType", result.getType());
            data.put("weekTypeLabel", result.getTypeLabel());

            return Result.success(data);
        } catch (Exception e) {
            com.lzy.lostandfound.utils.LogUtil.error("执行异常", e);
            return Result.error("生成光荣榜失败: " + e.getMessage());
        }
    }
}

