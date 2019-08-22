package com.nsg.mapper

import com.nsg.domain.Order
import com.nsg.domain.Payment
import com.nsg.domain.SubOrder

/**
 * Created by lijc on 16/3/11.
 */

interface OrderMapper {

    fun findAll(map: Map<String, Any?>): List<Order>?

    //    fun getOrdersByUser(userId: Long, type: String?, timestamp: Long?): List<Order>

    fun save(order: Order): Int

    fun saveSub(subOrder: SubOrder): Int

    fun getSkuStore(skuId: Long): Int?

    fun getById(orderId: Long): Order?

    fun changeOrderItemType(map: Map<String, Any>)

    fun changeOrderType(map: Map<String, Any>)

    fun reduceStore(map: Map<String, Any>)

    fun updateOrderBySN(payment: Payment)

    fun getItemTypes(orderId: Long): List<String?>?

    fun increaseSalesNum(map: Map<String, Any>)

    fun reduceSalesNum(map: Map<String, Any>)

    fun increaseStore(map: Map<String, Any>)

    fun getByOrderSN(orderSN: String): Order?

    fun getOrdersByType(orderType: String): List<Order>

    fun updateMessageStatus(orderId: Long)

    fun emptyPayType(orderId: Long)

    fun getShippingCode(orderId: Long): String?

    //    fun increaseStoreByOrderId(map: Map<String, Any>)

    //    fun saveCartItem(cartItem: CartItem): Int

}