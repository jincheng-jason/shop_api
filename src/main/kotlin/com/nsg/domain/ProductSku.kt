package com.nsg.domain

/**
 * Created by lijc on 16/3/12.
 */

data class ProductSku(
        var productSkuId: Long,
        var productId: Long,
        var purchasePrice: Int,
        var salesNum: Int,
        var storeNum: Int,
        var product_img: String,
        var show_price: Int,
        var sales_price: Int
) {
    private constructor() : this(0, 0, 0, 0, 0, "", 0, 0)
}