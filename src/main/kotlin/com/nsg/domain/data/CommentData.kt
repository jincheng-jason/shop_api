package com.nsg.domain.data

import com.alibaba.fastjson.JSON
import com.nsg.domain.Comment
import com.nsg.domain.Image
import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/2/26.
 */

data class CommentData(
        var commentId: Long,
        var userId: Long,
        var goodsId: Long,
        var rate: Int,
        var content: String,
        var purchaseTime: String,
        var commentTime: String,
        var timestamp: Long,
        var userImageUrl: String, // 冗余 用户头像
        var userNickName: String, // 冗余 用户昵称
        var skuAttrs: Array<SkuAttrData>?, // 冗余 评论的商品的sku属性,为json串转为data
        var images: List<Image>?
){
    companion object{
        fun of(source: Comment) = CommentData(source.commentId,source.userId,source.goodsId,source.rate,source.content,
                source.purchaseTime!!.dateFormatLongToString(), source.commentTime!!.dateFormatLongToString(), source.commentTime!!,
                source.userImageUrl, source.userNickName, JSON.parseObject(source.skuProperty, Array<SkuAttrData>::class.java), source.images)

        fun array(comments: List<Comment>): Array<CommentData?> {
            var commentDatas = arrayOfNulls<CommentData?>(0)
            comments?.forEach { commentDatas = commentDatas.plus(CommentData.of(it)) }
            return commentDatas
        }
    }
}