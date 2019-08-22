package com.nsg.mapper

import com.nsg.domain.OrderItem

/**
 * Created by lijc on 16/3/21.
 */

interface OrderItemMapper {

    fun batchSave(orderItemArray: List<OrderItem>): Int

    fun getOrderItemsByOrderId(orderId: Long): List<OrderItem>

    fun getOrderItemsByOrderSn(orderSN: String): List<OrderItem>

    fun updateItemType(map: Map<String, Any>)

}