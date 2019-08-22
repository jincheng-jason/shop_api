package com.nsg.domain

/**
 * Created by lijc on 16/3/12.
 */

data class CartItem(
        var cartItemId: Long,
        var cartId: Long,
        var skuId: Long,
        var buyNum: Int,
        var goodsId: Long,
        var goods: Goods?,
        var salesPrice: Int,
        var state: Boolean
) {
    private constructor() : this(0, 0, 0, 0, 0, null, 0, false)

    var salesPriceShow: Double = 0.0
        get() = salesPrice.toDouble() / 100
}