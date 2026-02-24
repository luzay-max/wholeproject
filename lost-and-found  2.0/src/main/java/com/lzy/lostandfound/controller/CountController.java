package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lzy.lostandfound.entity.Count;
import com.lzy.lostandfound.entity.FindInfo;
import com.lzy.lostandfound.entity.LostInfo;
import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.service.IFindInfoService;
import com.lzy.lostandfound.service.ILostInfoService;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/statistics")
public class CountController {
    @Autowired
    private IFindInfoService findInfoService;
    @Autowired
    private ILostInfoService lostInfoService;
    @Autowired
    private IUserService userService;
    @GetMapping
    public Result<Count> statistics() {
        Count count = new Count();
        //招领信息统计
        QueryWrapper<FindInfo> queryWrapper = new QueryWrapper<>();
        QueryWrapper<LostInfo> queryWrapper2 = new QueryWrapper<>();


        queryWrapper.eq("status", "APPROVED");
        count.setFindCount(findInfoService.count(queryWrapper));
        //失物信息统计
        queryWrapper2.eq("status", "APPROVED");
        count.setLostCount(lostInfoService.count(queryWrapper2));
        queryWrapper.clear();
        queryWrapper2.clear();
        //已解决信息统计
        queryWrapper.eq("status", "SOLVED");
        queryWrapper2.eq("status", "SOLVED");
        count.setSolvedCount(lostInfoService.count(queryWrapper2) + findInfoService.count(queryWrapper));
        queryWrapper.clear();
        queryWrapper2.clear();
        //拒绝信息统计
        queryWrapper.eq("status", "REJECTED");
        queryWrapper2.eq("status", "REJECTED");
        count.setRejectCount(lostInfoService.count(queryWrapper2) + findInfoService.count(queryWrapper));
        queryWrapper.clear();
        queryWrapper2.clear();
        //待审核信息统计
        queryWrapper.eq("status", "PENDING");
            queryWrapper2.eq("status", "PENDING");
        count.setPendingCount(lostInfoService.count(queryWrapper2) + findInfoService.count(queryWrapper));
        queryWrapper.clear();
        queryWrapper2.clear();
        //用户统计
        count.setUserCount(userService.count());

        return Result.success(count);
    }


}
