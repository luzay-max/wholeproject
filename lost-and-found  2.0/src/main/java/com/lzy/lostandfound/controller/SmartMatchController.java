package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.service.ISmartMatchService;
import com.lzy.lostandfound.vo.MatchCandidateVO;
import com.lzy.lostandfound.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/match")
public class SmartMatchController {

    @Autowired
    private ISmartMatchService smartMatchService;

    @GetMapping("/recommend")
    public Result recommend(@RequestParam String itemType,
                            @RequestParam String itemId,
                            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        if (!StringUtils.hasText(itemType) || !StringUtils.hasText(itemId)) {
            return Result.error("参数不完整");
        }
        List<MatchCandidateVO> list = smartMatchService.recommend(itemType, itemId, limit);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        return Result.success(data);
    }
}

