package com.lzy.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.lostandfound.dto.CommentCreateRequest;
import com.lzy.lostandfound.entity.Comment;
import com.lzy.lostandfound.entity.CommentLike;
import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.service.ICommentLikeService;
import com.lzy.lostandfound.service.ICommentService;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import com.lzy.lostandfound.anno.Log;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论接口
 * @author lzy
 * @since 2025-10-16
 */
@Validated
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private ICommentLikeService commentLikeService;
    @Autowired
    private IUserService userService;

    /**
     * 添加评论
     */
    @PostMapping("/add")
    @Log("ADD_COMMENT")
    public Result addComment(@Valid @RequestBody CommentCreateRequest request) {
        Map<String, Object> userMap = ThreadLocalUtil.get();
        String userId = (String) userMap.get("id");

        Comment comment = new Comment();
        comment.setInfoId(request.getInfoId());
        comment.setInfoType(request.getInfoType().toLowerCase());
        comment.setContent(request.getContent());
        comment.setId(UUID.randomUUID().toString());
        comment.setUserId(userId);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUsername((String) userMap.get("username"));
        comment.setAvatar(userService.getById(userId).getAvatar());

        boolean saved = commentService.save(comment);
        return saved ? Result.success("评论成功") : Result.error("评论失败");
    }

    /**
     * 获取评论列表（分页）
     */
    @GetMapping("/list")
    public Result getComments(@RequestParam @NotBlank String infoId,
                              @RequestParam @Pattern(regexp = "(?i)^(lost|find)$", message = "infoType must be lost or find") String infoType,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "20") Integer pageSize) {

        String normalizedType = infoType.toLowerCase();
        Page<Comment> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getInfoId, infoId)
                .eq(Comment::getInfoType, normalizedType)
                .orderByDesc(Comment::getCreateTime);

        commentService.page(pageInfo, queryWrapper);

        List<Comment> records = pageInfo.getRecords();
        if (!records.isEmpty()) {
            List<String> userIds = records.stream()
                    .map(Comment::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                List<User> users = userService.listByIds(userIds);
                Map<String, User> userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

                for (Comment c : records) {
                    User u = userMap.get(c.getUserId());
                    if (u != null) {
                        c.setAccountName(u.getAccountName());
                        // Optional: update avatar/username to latest
                        c.setAvatar(u.getAvatar()); 
                        c.setUsername(u.getUsername());
                    }
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getRecords());
        data.put("total", pageInfo.getTotal());

        return Result.success(data);
    }

    /**
     * 删除评论（仅作者可删）
     */
    @DeleteMapping("delete/{id}")
    @Log("DELETE_COMMENT")
    public Result deleteComment(@PathVariable String id) {
        Map<String, Object> userMap = ThreadLocalUtil.get();
        String userId = (String) userMap.get("id");

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        String role = userService.getById(userId).getRole();
        if (!(comment.getUserId().equals(userId) || (role != null && role.equalsIgnoreCase("ADMIN")))) {
            return Result.forbidden("无权限删除此评论");
        }

        boolean removed = commentService.removeById(id);
        return removed ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 点赞 / 取消点赞
     */
    @PostMapping("/like/{id}")
    @Log("LIKE_COMMENT")
    public Result toggleLike(@PathVariable String id) {
        Map<String, Object> userMap = ThreadLocalUtil.get();
        String userId = (String) userMap.get("id");

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, id)
                .eq(CommentLike::getUserId, userId);

        CommentLike exist = commentLikeService.getOne(wrapper, false);

        if (exist != null) {
            // 取消点赞
            commentLikeService.remove(wrapper);
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentService.updateById(comment);
            return Result.success("取消点赞成功");
        } else {
            // 新增点赞
            CommentLike like = new CommentLike();
            like.setId(UUID.randomUUID().toString());
            like.setCommentId(id);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            commentLikeService.save(like);

            comment.setLikeCount(comment.getLikeCount() + 1);
            commentService.updateById(comment);
            return Result.success("点赞成功");
        }
    }



}
