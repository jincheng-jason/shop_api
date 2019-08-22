package com.nsg.domain.data

import com.nsg.domain.Address
import com.nsg.domain.Order
import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/2/25.
 */

data class OrderData(
        var orderId: Long,
        var orderSN: String,
        var shopId: Long,
        var userId: Long,
        var createTime: String,
        var timestamp: Long,
        //        var productAmount: Int,    //商品总价
        //        var orderAmount: Int,      //订单总价(包含运费)
        var type: String,
        var goodsPrice: Int,      //商品的总价(不含邮费)
        var postage: Int,         //邮费
        var totalPrice: Int,      //含邮费的总价
        var address: Address?,
        var goodsArray: Array<CartItemData>?
){
    companion object{
        fun of(order: Order, goodsArray: Array<CartItemData>) =
                OrderData(order.orderId, order.orderSN, order.shopId, order.userId, order.createTime.dateFormatLongToString(), order.createTime, order.type, order.goodsPrice, order.postage, order.totalPrice, order.address, goodsArray)

        fun array(orders: Array<Order>) {

        }
    }
}