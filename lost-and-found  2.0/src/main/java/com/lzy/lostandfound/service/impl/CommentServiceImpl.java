package com.lzy.lostandfound.service.impl;

import com.lzy.lostandfound.entity.Comment;
import com.lzy.lostandfound.mapper.CommentMapper;
import com.lzy.lostandfound.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-10-16
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

}
