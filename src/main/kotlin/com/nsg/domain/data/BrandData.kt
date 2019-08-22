package com.nsg.domain.data

import com.nsg.domain.Brand

/**
 * Created by lijc on 16/2/26.
 */

data class BrandData(
        var brandId: Long,
        var brandName: String,
        var brandPic: String,
        var brandSort: Int,
        var brandRecommend: Int
){
    companion object{
        fun of(source: Brand) = BrandData(source.brandId,source.brandName,source.brandPic,source.brandSort,source.brandRecommend)
    }
}