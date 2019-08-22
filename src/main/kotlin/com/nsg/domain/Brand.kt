package com.nsg.domain

/**
 * Created by lijc on 16/2/23.
 */

data class Brand(
        var brandId: Long,
        var brandName: String,
        var brandPic: String,
        var brandSort: Int,
        var brandRecommend: Int
)