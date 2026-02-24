package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.utils.AliOssUtil;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")

public class FileUploadController {

    @Autowired
    private IUserService userService;

    /**
     * 上传用户头像
     */
    @PostMapping("/updateAvatar")
    public Result<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请上传头像文件");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

            // 上传到 OSS
            String url = AliOssUtil.uploadFile(fileName, file.getInputStream());

            // 更新数据库
            Map<String, Object> claims = ThreadLocalUtil.get();
            String userId = (String) claims.get("id");
            if (userId == null) {
                return Result.error("用户未登录或身份失效");
            }

            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            user.setAvatar(url);
            userService.updateById(user);

            // 返回上传成功的 URL
            return Result.success(url);

        } catch (Exception e) {
            e.printStackTrace(); // 保留日志供调试
            return Result.error("头像上传失败，请重试");
        }
    }


@PostMapping("/uploadImage")
    public Result<String> uploadImage(MultipartFile file) throws Exception {
        //文件存在本地磁盘
        //保证文件名唯一
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
//        file.transferTo(new File("C:\\Users\\Max\\Desktop\\files\\"+fileName));
        //调用工具类
        //上传到OSS
        //返回url地址
        String url =AliOssUtil.uploadFile(fileName, file.getInputStream());

        return Result.success(url);

    }

}
