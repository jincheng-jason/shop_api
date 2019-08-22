package com.nsg.mapper

import com.nsg.domain.SkuInfo

/**
 * Created by lijc on 16/3/27.
 */

interface SkuInfoMapper {

    fun getSkuInfos(goodsId: Long): List<SkuInfo>

    fun getSkuInfoById(skuId: Long): SkuInfo

    fun updateSkuState(map: Map<String, Any>)

    fun countSkuInPromotion(skuId: Long): Int

    fun countUserSkuInPromotion(map: Map<String, Any>): Int

}