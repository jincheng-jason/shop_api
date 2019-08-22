package com.nsg.domain.data

/**
 * Created by lijc on 16/3/3.
 */

data class CartItemData(
        //        var cartId: Long,
        var goods: OrderGoodsData?,
        var skuId: Long,
        var buyNum: Int,
        var cartItemId: Long
        //        var userId: Long?
)