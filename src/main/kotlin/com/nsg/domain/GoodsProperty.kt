package com.nsg.domain

/**
 * Created by lijc on 16/3/1.
 */

data class GoodsProperty(
        var goodsPropertyId: Long,
        var goodsId: Long,
        var propertyName: String,
        var propertyValue: String,
        var sort: Int,
        var createTime: Long
)