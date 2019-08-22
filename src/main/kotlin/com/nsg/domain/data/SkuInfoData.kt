package com.nsg.domain.data

import com.nsg.domain.SkuInfo

/**
 * Created by lijc on 16/3/7.
 */

data class SkuInfoData(
        var skuId: Long,
        var skuValue: String,
        var productId: Long,
        var purchasePrice: Long,
        var salesNum: Int,
        var storeNum: Int,
        var showPrice: Int,
        var salesPrice: Int,
        var productImg: String,
        var showPriceShow: Double,
        var salesPriceShow: Double,
        var goodsId: Long
) {
    companion object {
        fun of(skuInfo: SkuInfo, goodsId: Long): SkuInfoData {
            var skuAttrs = skuInfo.skuAttrs
            var skuValue = ""
            skuAttrs!!.forEach { skuValue += "|" + it.attrValue }
            skuValue = skuValue.replaceFirst("|", "")
            return SkuInfoData(skuInfo.skuId, skuValue, skuInfo.productId, skuInfo.purchasePrice, skuInfo.salesNum,
                    skuInfo.storeNum, skuInfo.showPrice, skuInfo.salesPrice, skuInfo.productImg, skuInfo.showPriceShow, skuInfo.salesPriceShow, goodsId)
        }
    }
}