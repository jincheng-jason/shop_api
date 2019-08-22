package com.nsg.domain

/**
 * Created by lijc on 16/3/30.
 */

data class ProductRefAttr(
        var productRefAttrId: Long,
        var productSkuId: Long,
        var productAttrId: Long
) {
    private constructor() : this(0, 0, 0)
}