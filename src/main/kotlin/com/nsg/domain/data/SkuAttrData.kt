package com.nsg.domain.data

/**
 * 订单中商品的一项确定的SKU属性
 * Created by lijc on 16/3/2.
 */

data class SkuAttrData(
        var attrName: String,
        var attrValue: String
        //        var attrImg: String
) {
    private constructor() : this("", "")
}