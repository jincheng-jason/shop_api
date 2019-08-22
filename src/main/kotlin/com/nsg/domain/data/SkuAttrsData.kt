package com.nsg.domain.data

/**
 * 一件商品的一项SKU属性的所有选项值
 * Created by lijc on 16/3/3.
 */

data class SkuAttrsData(
        var attrName: String,
        var attrValues: Array<SkuAttrData>
)