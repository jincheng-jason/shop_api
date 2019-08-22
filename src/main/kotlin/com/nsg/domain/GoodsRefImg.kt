package com.nsg.domain

/**
 * Created by lijc on 16/3/30.
 */

data class GoodsRefImg(
        var goodsRefImgId: Long,
        var goodsId: Long,
        var imgId: Long
) {
    private constructor() : this(0, 0, 0)
}