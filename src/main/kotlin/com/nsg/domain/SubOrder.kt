package com.nsg.domain

/**
 * Created by lijc on 16/3/12.
 */

data class SubOrder(
        var subOrderId: Long,
        var orderId: Long,
        var paymentId: Long?,
        var paymentName: String?,
        var paymentCode: String?,
        var paymentTime: Long?,
        var payMessage: String?,
        var subOrderState: Int, //默认0 0未支付， 1,已支付，未发货  2，发货， 3 已收货（完成） 4,退货
        var shippingTime: Long?,
        var shippingCode: String?,
        var finishedTime: Long?,
        var invoice: String?,
        var subGoodsPrice: Int, //子订单 商品总价
        var subPostage: Int, //子订单 邮费
        var subTotalPrice: Int, //子订单 总价
        var createTime: Long
) {
    private constructor() : this(0, 0, 0, "", "", 0, "", 0, 0, "", 0, "", 0, 0, 0, 0)
}