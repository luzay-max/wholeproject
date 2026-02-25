package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.utils.AliOssUtil;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/user")

public class FileUploadController {
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    );

    @Autowired
    private IUserService userService;

    /**
     * 上传用户头像
     */
    @PostMapping("/updateAvatar")
    public Result<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        String validationError = validateImageFile(file, "头像文件");
        if (validationError != null) {
            return Result.error(validationError);
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
        String validationError = validateImageFile(file, "图片文件");
        if (validationError != null) {
            return Result.error(validationError);
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
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID() + extension;
    }

    private String validateImageFile(MultipartFile file, String fileTypeLabel) {
        if (file == null || file.isEmpty()) {
            return "请上传" + fileTypeLabel;
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return fileTypeLabel + "名称不能为空";
        }
        String extension = getFileExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            return fileTypeLabel + "格式不正确，请上传 JPG/PNG/GIF/BMP/WEBP 格式图片";
        }
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            return fileTypeLabel + "格式不支持，请上传 JPG/PNG/GIF/BMP/WEBP 格式图片";
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return fileTypeLabel + "内容类型不正确，请重新上传";
        }
        return null;
    }

    private String getFileExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index);
    }
}
