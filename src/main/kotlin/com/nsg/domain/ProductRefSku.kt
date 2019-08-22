package com.nsg.domain

/**
 * Created by lijc on 16/3/30.
 */

data class ProductRefSku(
        var productsRefSkuId: Long,
        var productsId: Long,
        var productsSkuId: Long
) {
    private constructor() : this(0, 0, 0)
}