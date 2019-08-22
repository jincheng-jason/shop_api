package com.nsg.domain

/**
 * Created by lijc on 16/2/25.
 */

import com.nsg.util.dateFormatLongToString

class Order(
        var orderId: Long,
        var orderSN: String,
        var shopId: Long,
        var userId: Long, //用户id改为uuid
        var addressId: Long,
        var createTime: Long,
        var type: String, //type={unpay,unship,unrecv(未收货),unrate,rated,dealclose} 默认 unpay
        var goodsPrice: Int,      //商品的总价(不含邮费)
        var postage: Int,         //邮费
        var totalPrice: Int, //含邮费的总价
        var address: Address?,
        var goodsArray: List<OrderItem>?,
        var description: String?,
        var overdueTime: Long,
        var uuidUserId: String,
        var payType: String,
        var receivedTime: Long,
        var overdueMessageStatus: Boolean

) {

    private constructor() : this(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, null, 0, "", "", 0, false)

    var totalPriceShow: Double = 0.0
        get() = totalPrice.toDouble() / 100
    var postageShow: Double = 0.0
        get() = postage.toDouble() / 100
    var goodsPriceShow: Double = 0.0
        get() = goodsPrice.toDouble() / 100
    var createTimeShow: String = ""
        get() = createTime.dateFormatLongToString()
    var overdueTimeShow: String = ""
        get() = overdueTime.dateFormatLongToString()
}