package com.nsg.mapper

import com.nsg.domain.CartItem
import com.nsg.domain.form.CartItemForm

/**
 * Created by lijc on 16/3/28.
 */
interface CartItemMapper {

    fun getByCartId(cartId: Long): List<CartItem>

    fun getById(cartItemId: Long): CartItem

    fun putInCart(cartItem: CartItem): Int

    fun batchDeleteCartItems(cartItemIds: List<Long>)

    fun clearCart(cartId: Long)

    fun getCartItemByIdAndSku(map: Map<String, Long>): CartItem

    fun increaseBuyNum(map: Map<String, Any>)

    fun updateCartItemBuyNum(cartItem: CartItemForm)
}