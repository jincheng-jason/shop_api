package com.nsg.service;

import com.nsg.domain.Comment;
import com.nsg.mapper.CommentMapper;
import com.nsg.mapper.ImageMapper;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by lijc on 16/3/23.
 */

@Transactional
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ImageMapper imageMapper;

    @Nullable
    public Comment getById(long comment_id) {
        return commentMapper.getById(comment_id);
    }

    public List<Comment> getCommentsByGoods(Map<String, Object> map) {
        return commentMapper.getCommentsByGoods(map);
    }

    public int save(Comment comment) {
        return commentMapper.save(comment);
    }

    @Nullable
    public List<Integer> getGoodsCommentCounts(long goods_id) {
        return commentMapper.getGoodsCommentCounts(goods_id);
    }

    @Nullable
    public List<Comment> getGoodCommentsByGoods(Map<String, Object> map) {
        return commentMapper.getGoodCommentsByGoods(map);
    }

    @Nullable
    public List<Comment> getNormalCommentsByGoods(Map<String, Object> map) {
        return commentMapper.getNormalCommentsByGoods(map);
    }

    @Nullable
    public List<Comment> getBadCommentsByGoods(Map<String, Object> map) {
        return commentMapper.getBadCommentsByGoods(map);
    }

    @Nullable
    public List<Comment> getPicCommentsByGoods(Map<String, Object> map) {
        return commentMapper.getPicCommentsByGoods(map);
    }

    public void deleteComment(long commentId) {
        commentMapper.deleteComment(commentId);
    }
}
