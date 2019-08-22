package com.nsg.domain

/**
 * Created by lijc on 16/3/30.
 */

data class Props(
        var propsId: Long,
        var sn: String,
        var state: Int,
        var productId: Long,
        var skuId: Long,
        var createTime: Long
) {
    private constructor() : this(0, "", 0, 0, 0, 0)
}
