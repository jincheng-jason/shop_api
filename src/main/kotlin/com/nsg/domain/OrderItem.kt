package com.nsg.domain

import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/3/12.
 */

data class OrderItem(
        var orderItemId: Long,
        var subOrderId: Long,
        var skuId: Long,
        var goods: Goods?,
        var buyNum: Int,
        var goodsId: Long,
        var goodsName: String,
        var salesPrice: Int,
        var createTime: Long,
        var itemType: String, //rated为已评价
        var isVirtual: Boolean
) {
    private constructor() : this(0, 0, 0, null, 0, 0, "", 0, 0, "", true)

    var salesPriceShow: Double = 0.0
        get() = salesPrice.toDouble() / 100
    var createTimeShow: String = ""
        get() = createTime.dateFormatLongToString()
}