package com.nsg.mapper

import com.nsg.domain.Comment

/**
 * Created by lijc on 16/3/10.
 */

interface CommentMapper {

    fun getById(commentId: Long): Comment?

    fun getCommentsByGoods(map: Map<String, Any?>): List<Comment>?

    fun save(comment: Comment): Int

    fun getGoodsCommentCounts(goodsId: Long): List<Int>

    fun getGoodCommentsByGoods(map: Map<String, Any?>): List<Comment>?

    fun getNormalCommentsByGoods(map: Map<String, Any?>): List<Comment>?

    fun getBadCommentsByGoods(map: Map<String, Any?>): List<Comment>?

    fun getPicCommentsByGoods(map: Map<String, Any?>): List<Comment>?

    fun deleteComment(commentId: Long)

}