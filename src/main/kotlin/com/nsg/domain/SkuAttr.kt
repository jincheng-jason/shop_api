package com.nsg.domain

/**
 * Created by lijc on 16/3/16.
 */

data class SkuAttr(
        var skuAttrId: Long,
        var attrName: String,
        var attrValue: String
) {
    private constructor() : this(0, "", "")
}