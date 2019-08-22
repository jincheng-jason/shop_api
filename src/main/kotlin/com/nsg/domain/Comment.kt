package com.nsg.domain

import com.alibaba.fastjson.JSON
import com.nsg.domain.data.SkuAttrData
import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/2/23.
 */

data class Comment(
        var commentId: Long,
        var userId: Long,
        var orderId: Long,
        var goodsId: Long,
        var skuId: Long,
        var rate: Int,
        var content: String,
        var purchaseTime: Long?,
        var commentTime: Long?,
        var userImageUrl: String, // 冗余 用户头像
        var userNickName: String, // 冗余 用户昵称
        var skuProperty: String?, // 冗余 评论的商品的sku属性,为json串
        var images: List<Image>?,
        var createTime: Long?
) {
    private constructor() : this(0, 0, 0, 0, 0, 0, "", 0, 0, "", "", "", null, 0)

    var createTimeShow: String = ""
        get() = createTime!!.dateFormatLongToString()
    var purchaseTimeShow: String = ""
        get() = purchaseTime!!.dateFormatLongToString()
    var commentTimeShow: String = ""
        get() = commentTime!!.dateFormatLongToString()
    var skuPropertyShow: Array<SkuAttrData> = emptyArray()
        get() {
            if (!skuProperty.isNullOrBlank()) {
                return JSON.parseObject(skuProperty, Array<SkuAttrData>::class.java)
            } else {
                return emptyArray()
            }
        }
}