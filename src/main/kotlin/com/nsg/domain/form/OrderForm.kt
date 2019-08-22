package com.nsg.domain.form

import com.nsg.domain.Address

/**
 * Created by lijc on 16/2/25.
 */

class OrderForm{
    var orderId: Long? = 0
    var orderSN: String? = ""
    var shopId: Long? = 0
    var cartId: Long = 0
    var address: Address? = null
    var addressId: Long = 0
    //cartItems 包含 skuId(最小粒度),goodsId(包含一个商品的所有SKU),buyNum
    var cartItems: List<CartItemForm> = emptyList()
    var goodsPrice: Int = 0      //商品的总价(不含邮费)
    var postage: Int = 0         //邮费
    var totalPrice: Int = 0      //含邮费的总价
    var userId: Long? = 0
    var type: String? = null
    var overdueTime: Long? = null
    var uuidUserId: String? = ""
    var description: String? = ""
}