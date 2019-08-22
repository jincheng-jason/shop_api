package com.nsg.domain.data

/**
 * Created by lijc on 16/3/10.
 */

data class CartData(
        var cartId: Long,
        var userId: Long?,
        var cartItems: Array<CartItemData>
)