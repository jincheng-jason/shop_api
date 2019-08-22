package com.nsg.mapper

import com.nsg.domain.Props

/**
 * Created by lijc on 16/3/30.
 */
interface PropsMapper {

    fun getBySkuId(skuId: Long): Props

    fun updateWaste(map: Map<String, Any>)

    fun getPropsWasteCount(map: Map<String, Any>): Int

}