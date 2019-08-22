package com.nsg.domain.data

/**
 * Created by lijc on 16/3/1.
 */

data class ShippingData(
        var shippingFee: Int
) {
    var shippingFeeShow: Double = 0.0
        get() = shippingFee.toDouble() / 100
    var shippingCompany: String = "宅急送"
}
