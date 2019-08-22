package com.nsg.mapper

import com.nsg.domain.SkuAttr

/**
 * Created by lijc on 16/3/23.
 */

interface SkuAttrMapper {

    fun getSkuAttrs(skuId: Long): List<SkuAttr>

    fun getGoodsAttrs(goodsId: Long): List<SkuAttr>

}