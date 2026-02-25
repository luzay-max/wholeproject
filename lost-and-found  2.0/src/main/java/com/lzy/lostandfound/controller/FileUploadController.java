package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.utils.AliOssUtil;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
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
            String fileName = buildUniqueFileName(file);

            // 上传到 OSS
            String url = AliOssUtil.uploadFile(fileName, file.getInputStream());

            // 更新数据库
            Map<String, Object> claims = ThreadLocalUtil.get();
            String userId = claims != null && claims.get("id") != null ? String.valueOf(claims.get("id")) : null;
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
            log.error("头像上传失败", e);
            return Result.error("头像上传失败，请重试");
        }
    }


@PostMapping("/uploadImage")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请上传图片文件");
        }
        try {
            String fileName = buildUniqueFileName(file);
            String url = AliOssUtil.uploadFile(fileName, file.getInputStream());
            return Result.success(url);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败，请重试");
        }
    }

    private String buildUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null) {
            int index = originalFilename.lastIndexOf(".");
            if (index >= 0 && index < originalFilename.length() - 1) {
                extension = originalFilename.substring(index);
            }
        }
        return UUID.randomUUID() + extension;
    }
}
