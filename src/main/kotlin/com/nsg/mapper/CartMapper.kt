package com.nsg.mapper

import com.nsg.domain.Cart

/**
 * Created by lijc on 16/3/28.
 */

interface CartMapper {

    fun getCartByUser(map: Map<String, Any?>): Cart?

    fun newCartForUser(cart: Cart): Int

    fun cartCount(cartId: Long): Int

    fun cartCountOfUser(map: Map<String, Any?>): Int
}