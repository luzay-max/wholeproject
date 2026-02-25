package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.dto.RegisterRequest;
import com.lzy.lostandfound.dto.UserUpdateRequest;
import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.entity.StudentWhitelist;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.service.IStudentWhitelistService;
import com.lzy.lostandfound.utils.JwtUtil;
import com.lzy.lostandfound.utils.Md5Util;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.vo.Tu;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
@Tag(name = "用户管理")
@Validated
@RestController
public class UserController {
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_TTL_MINUTES = 2;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$");
    @Autowired
    private IUserService userService;
    @Autowired
    private IStudentWhitelistService studentWhitelistService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @GetMapping("/auth/captcha")
    public Result getCaptcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String code = lineCaptcha.getCode();
        String captchaId = UUID.randomUUID().toString().replace("-", "");

        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(CAPTCHA_KEY_PREFIX + captchaId, code.toLowerCase(), CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        lineCaptcha.write(outputStream);
        String imageBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        Map<String, String> data = new HashMap<>();
        data.put("captchaId", captchaId);
        data.put("imageBase64", imageBase64);
        return Result.success(data);
    }

    @GetMapping("/auth/check-username")
    public Result<Map<String, Object>> checkUsername(@RequestParam @NotBlank String username) {
        String value = username.trim();
        User existingUser = userService.query().eq("username", value).one();
        Map<String, Object> data = new HashMap<>();
        data.put("exists", existingUser != null);
        return Result.success(data);
    }
    @PostMapping("/auth/register")
    @Operation(summary = "register")
    @Log("REGISTER")
    public Result register(@Valid @RequestBody RegisterRequest request) {

         String username = request.getUsername() == null ? "" : request.getUsername().trim();
         String phone = request.getPhone() == null ? "" : request.getPhone().trim();
         String email = request.getEmail() == null ? "" : request.getEmail().trim();
         String studentId = request.getStudentId() == null ? "" : request.getStudentId().trim();
         String name = request.getName() == null ? "" : request.getName().trim();

         if (!request.getPassword().equals(request.getConfirmPassword())) {
             return Result.error("两次输入密码不一致");
         }

         StudentWhitelist wl = studentWhitelistService.query()
                 .eq("student_id", studentId)
                 .eq("name", name)
                 .one();
         if (wl == null) {
             return Result.error("不在白名单中");
         }

         if (existsByColumn("username", username)) {
             return Result.error("用户名已存在");
         }
         if (existsByColumn("account_name", username)) {
             return Result.error("登录账号已存在");
         }
         if (existsByColumn("student_id", studentId)) {
             return Result.error("学号/工号已存在");
         }
         if (existsByColumn("phone", phone)) {
             return Result.error("手机号已存在");
         }
         if (existsByColumn("email", email)) {
             return Result.error("邮箱已存在");
         }

         User user = new User();
         user.setUsername(username);
         user.setAccountName(username);
         user.setPassword(request.getPassword());
         user.setPhone(phone);
         user.setEmail(email);
         user.setName(name);
         user.setStudentId(studentId);
         user.setCollege(wl.getCollege());
         user.setStatus(0);
         user.setRole(request.getRole().toUpperCase());
         user.setCreateTime(LocalDateTime.now());
         user.setUpdateTime(LocalDateTime.now());

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String bcrypt = encoder.encode(user.getPassword());
        user.setPassword(bcrypt);
        userService.save(user);
        return Result.success();



    }
    @PostMapping("/auth/login")
    @Log("LOGIN")
    public Result login(@RequestParam(required = false) String accountName,
                        @RequestParam(required = false) String username,
                        @RequestParam @NotBlank String password,
                        @RequestParam @NotBlank String captchaId,
                        @RequestParam @NotBlank String captchaCode,
                        @RequestParam(required = false, defaultValue = "false") boolean rememberMe) {
        if (captchaId == null || captchaCode == null || captchaCode.isBlank()) {
            return Result.error("验证码不能为空");
        }
        String captchaKey = CAPTCHA_KEY_PREFIX + captchaId;
        ValueOperations<String, String> captchaOperations = redisTemplate.opsForValue();
        String cachedCode = captchaOperations.get(captchaKey);
        if (cachedCode == null) {
            return Result.error("验证码已过期");
        }
        if (!cachedCode.equalsIgnoreCase(captchaCode.trim())) {
            return Result.error("验证码错误");
        }
        redisTemplate.delete(captchaKey);
        User u = null;
        if (accountName != null && !accountName.isBlank()) {
            u = userService.query().eq("account_name", accountName.trim()).one();
        }
        if (u == null && username != null && !username.isBlank()) {
            u = userService.query().eq("username", username.trim()).one();
        }
        if (u == null) {
            return Result.error("账户不存在");
        }
        
        if (u.getStatus() != null && u.getStatus() == 1) {
            return Result.error("账户已被封禁，请联系管理员");
        } else {
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            boolean ok = encoder.matches(password, u.getPassword());
            if (!ok) {
                if (Md5Util.checkPassword(password, u.getPassword())) {
                    String upgraded = encoder.encode(password);
                    u.setPassword(upgraded);
                    u.setUpdateTime(LocalDateTime.now());
                    userService.updateById(u);
                    ok = true;
                }
            }
            if (!ok) {
                return Result.error("密码错误");
            }
            Map<String,Object> claims=new HashMap<>();
            claims.put("username",u.getUsername());
            claims.put("id",u.getId());
            String token  = JwtUtil.genToken(claims);
            //
            //把token存入redis种
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(token, token, 12, TimeUnit.HOURS);//等于token过期时间`
            Tu tu=new Tu();
            tu.setToken(token);
            tu.setUser(u);
            if (rememberMe) {
                String refreshToken = UUID.randomUUID().toString().replace("-", "");
                operations.set("refresh:" + refreshToken, u.getId(), 7, TimeUnit.DAYS);
                tu.setRefreshToken(refreshToken);
            }
            return Result.success(tu);
        }
    }
    @PostMapping("/auth/refresh")
    public Result refresh(@RequestParam @NotBlank String refreshToken) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String userId = operations.get("refresh:" + refreshToken);
        if (userId == null) {
            return Result.error("Refresh Token 无效或已过期");
        }
        User u = userService.getById(userId);
        if (u == null) {
            return Result.error("用户不存在");
        }
        
        if (u.getStatus() != null && u.getStatus() == 1) {
            return Result.error("账户已被封禁，请联系管理员");
        }
        
        Map<String,Object> claims=new HashMap<>();
        claims.put("username",u.getUsername());
        claims.put("id",u.getId());
        String token  = JwtUtil.genToken(claims);
        operations.set(token, token, 12, TimeUnit.HOURS);
        Tu tu=new Tu();
        tu.setToken(token);
        tu.setUser(u);
        tu.setRefreshToken(refreshToken);
        return Result.success(tu);
    }
    @PostMapping("/auth/logout")
    @Log("LOGOUT")
    public Result logout(HttpServletRequest request){
        String token = request.getHeader("Authorization");


        if (token == null) {
            return Result.error("redis异常");
        }
            String andDelete = redisTemplate.opsForValue().getAndDelete(token);
        if (andDelete == null) {
            return Result.error("登出失败");
        }

        return Result.success();

    }
    @PostMapping("/auth/resetPassword")
    @Log("UPDATE_PASSWORD")
    public Result resetPassword(@RequestParam(required = false) String oldPassword,
                                @RequestParam(required = false) String newPassword,
                                @RequestBody(required = false) Map<String, String> body) {
        try {
            String oldPwd = oldPassword;
            String newPwd = newPassword;
            if (body != null) {
                if ((oldPwd == null || oldPwd.isBlank())) {
                    oldPwd = body.get("oldPassword");
                }
                if ((newPwd == null || newPwd.isBlank())) {
                    newPwd = body.get("newPassword");
                }
            }

            if (oldPwd == null || oldPwd.isBlank()) {
                return Result.error("原密码不能为空");
            }
            if (newPwd == null || newPwd.isBlank()) {
                return Result.error("新密码不能为空");
            }
            String nextPassword = newPwd.trim();
            if (!PASSWORD_PATTERN.matcher(nextPassword).matches()) {
                return Result.error("新密码需8-20位，且包含字母和数字");
            }

            Map<String,Object> map = ThreadLocalUtil.get();
            String userId = map.get("id").toString();
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            boolean verified = false;
            try {
                verified = encoder.matches(oldPwd, user.getPassword());
            } catch (Exception ignored) {}
            if (!verified) {
                if (Md5Util.checkPassword(oldPwd, user.getPassword())) {
                    verified = true;
                }
            }
            if (!verified) {
                return Result.error("原密码错误");
            }
            String bcryptPassword = encoder.encode(nextPassword);
            user.setPassword(bcryptPassword);
            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);
            return Result.success("密码已更新");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新密码失败");
        }
    }
    @GetMapping("/user/info")
    public Result getUserInfo() {
        // 从token中获取用户信息
       Map<String,Object> map= ThreadLocalUtil.get();
       map.get("username");
      User user =   userService.query().eq("username",map.get("username")).one();

        return Result.success(user);     // 这里返回用户信息
    }
    @PostMapping("/user/update")
    @Log("UPDATE_USER_INFO")
    public Result updateUser(@Valid @RequestBody UserUpdateRequest userUpdate) {
        try {
            // 从token中获取当前登录用户ID
            Map<String,Object> map = ThreadLocalUtil.get();
            String userId = map.get("id").toString();

            // 获取当前用户信息
            User currentUser = userService.getById(userId);
            if (currentUser == null) {
                return Result.error("用户不存在");
            }

            // 更新字段 - 确保处理前端传递的所有可能字段
            // 真实姓名不可更改
            // if (userUpdate.getName() != null) {
            //     currentUser.setName(userUpdate.getName());
            // }
            if (userUpdate.getEmail() != null) {
                currentUser.setEmail(userUpdate.getEmail());
            }
            if (userUpdate.getPhone() != null) {
                currentUser.setPhone(userUpdate.getPhone());
            }
            // 所在院系不可更改
            // if (userUpdate.getCollege() != null) {
            //    currentUser.setCollege(userUpdate.getCollege());
            // }
            if (userUpdate.getUsername() != null) {
                String targetUsername = userUpdate.getUsername().trim();
                if (targetUsername.isEmpty()) {
                    return Result.error("用户名不能为空");
                }
                // 检查用户名是否已被占用
                User existingUser = userService.query().eq("username", targetUsername).ne("id", userId).one();
                if (existingUser != null) {
                    return Result.error("用户名已被占用");
                }
                currentUser.setUsername(targetUsername);
            }

            currentUser.setUpdateTime(LocalDateTime.now());

            userService.updateById(currentUser);

            // 返回更新后的用户信息，不包含密码
            currentUser.setPassword(null);
            return Result.success(currentUser);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新用户信息失败");
        }
    }

    private boolean existsByColumn(String column, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return userService.query().eq(column, value).count() > 0;
    }

}
