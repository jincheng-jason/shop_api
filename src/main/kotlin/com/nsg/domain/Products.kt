package com.nsg.domain

/**
 * Created by lijc on 16/2/23.
 */

data class Products(
        var productId: Long,
        var productBn: String,
        var productName: String,
        var purchaseTime: Long,
        var purchaseNum: Long
) {
    private constructor() : this(0, "", "", 0, 0)
}