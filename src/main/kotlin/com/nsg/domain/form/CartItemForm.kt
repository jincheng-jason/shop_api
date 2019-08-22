package com.nsg.domain.form

import com.nsg.domain.Goods

/**
 * Created by lijc on 16/3/3.
 */

class CartItemForm {
    var cartId: Long? = null
    var cartItemId: Long = 0
    var goodsId: Long = 0
    var skuId: Long = 0
    var buyNum: Int = 0
    var userId: Long? = null
    var goods: Goods? = null
    var addressId: Long = 0
}