package com.nsg.domain.data

import com.nsg.domain.Goods
import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/3/2.
 */

data class OrderGoodsData(
        var goodsId: Long,
        var productId: Long,
        var skuId: Long,
        var shopId: Long,
        var goodsName: String,
        var goodsIntro: String,
        var mainPicUrl: String,
        var introUrl: String,
        //        var showPrice: Int,
        //        var salesPrice: Int,
        var goodsShow: Int,
        var goodsClick: Long,
        var goodsState: Int,
        var goodsCommend: Int, //是否推荐
        var createTime: String,
        // var commentNum: Long,
        var saleNum: Long,
        var storeNum: Long,
        var skuAttrs: Array<SkuAttrData>
) {
    companion object {
        fun of(source: Goods, skuId: Long, skuAttrs: Array<SkuAttrData>) =
                OrderGoodsData(source.goodsId, source.productId, skuId,
                        source.shopId, source.goodsName, source.goodsIntro, source.mainPicUrl,
                        source.infoUrl, source.goodsShow,
                        source.goodsClick, source.goodsState, source.goodsCommend,
                        source.createTime.dateFormatLongToString(), source.saleNum, source.storeNum, skuAttrs)
    }
}