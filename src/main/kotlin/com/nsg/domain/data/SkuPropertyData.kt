package com.nsg.domain.data

/**
 * Created by lijc on 16/3/8.
 */

data class SkuPropertyData(
        var skuAttrs: Array<SkuAttrsData>,
        var skuValues: Array<SkuInfoData>
) {
    companion object {
        fun of(skuAttrs: Array<SkuAttrsData>, skuValues: Array<SkuInfoData>) = SkuPropertyData(skuAttrs, skuValues)
    }
}