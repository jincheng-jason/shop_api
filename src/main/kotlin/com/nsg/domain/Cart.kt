package com.nsg.domain

/**
 * Created by lijc on 16/2/23.
 */

data class Cart(
        var cartId: Long = 0,
        var userId: Long? = 0,
        var shopId: Long? = 0,
        var promotionId: Long? = 0,
        //        var skuId: Long? = 0,
        //        var buyNum: Long? = 0,
        var cartItems: List<CartItem>? = null
) {
    private constructor() : this(0, 0, 0, 0, null)
}