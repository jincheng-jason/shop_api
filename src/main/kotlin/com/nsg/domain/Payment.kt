package com.nsg.domain

/**
 * Created by lijc on 16/3/21.
 */

data class Payment(
        var paymentId: Long,
        var shopId: Long,
        var paymentStatus: Int,
        var orderSN: String,
        var paymentPrice: Int,
        var paymentTime: Long,
        var payType: String,
        var createTime: Long,
        var paymentCode: String?,
        var paymentName: String?,
        var paymentInfo: String?,
        var paymentConfig: String?,
        var paymentSort: Int?
) {
    private constructor() : this(0, 0, 0, "", 0, 0, "", 0, null, null, null, null, null)
}