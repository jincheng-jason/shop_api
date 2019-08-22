package com.nsg.domain

import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/2/22.
 */

data class Goods (
        var goodsId: Long,
        var productId: Long,
        //    var productSkuId: Long,
        var shopId: Long,
        var brandId: Long,
        var goodsName: String,
        var goodsIntro: String,
        var mainPicUrl: String, //在商品列表中显示的缩略图
        var infoUrl: String,
        var showPrice: String,
        var salesPrice: String,
        var goodsShow: Int,
        var goodsClick: Long,
        var goodsState: Int,
        var goodsCommend: Int, //是否推荐
        var createTime: Long,
    // var commentNum: Long,
        var saleNum: Long,
        var storeNum: Long,
        var skuAttrs: List<SkuAttr>?,
        var isVirtual: Boolean,
        var specUrl: String
) {
    private constructor() : this(0, 0, 0, 0, "", "", "", "", "", "", 0, 0, 0, 0, 0, 0, 0, null, true, "")

    var createTimeShow: String = ""
        get() = createTime.dateFormatLongToString()
    var showPriceShow: String = ""
        get() = showPrice
    var salesPriceShow: String = ""
        get() = salesPrice
}