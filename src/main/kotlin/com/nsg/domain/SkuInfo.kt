package com.nsg.domain

/**
 * Created by lijc on 16/3/27.
 */

data class SkuInfo(
        var skuId: Long,
        var productId: Long,
        var purchasePrice: Long,
        var salesNum: Int,
        var storeNum: Int,
        var showPrice: Int,
        var salesPrice: Int,
        var productImg: String,
        var skuAttrs: List<SkuAttr>?,
        var state: Boolean,
        var weight: Int
) {
    private constructor() : this(0, 0, 0, 0, 0, 0, 0, "", null, false, 0)

    var showPriceShow: Double = 0.0
        get() = showPrice.toDouble() / 100
    var salesPriceShow: Double = 0.0
        get() = salesPrice.toDouble() / 100
}