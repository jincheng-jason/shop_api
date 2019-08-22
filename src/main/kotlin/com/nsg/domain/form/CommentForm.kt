package com.nsg.domain.form

import com.nsg.domain.Image

/**
 * Created by lijc on 16/2/26.
 */

class CommentForm{
    var commentId: Long = 0
    var userId: Long = 0
    var goodsId: Long = 0
    var skuId: Long = 0
    var rate: Int = 0
    var content: String = ""
    var purchaseTime: Long = 0
    var commentTime: Long = 0
    var userImageUrl: String = "" // 冗余 用户头像
    var userNickName: String = "" // 冗余 用户昵称
    var skuProperty: String? = null // 冗余 评论的商品的sku属性,为json串
    var images: List<Image>? = null
}