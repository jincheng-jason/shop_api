package com.nsg.domain.data

import com.nsg.domain.Goods
import com.nsg.domain.Image
import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/2/26.
 */

data class GoodsData(
        var goodsId: Long,
        var productId: Long,
        //        var productSkuId: Long,
        var shopId: Long,
        var goodsName: String,
        var goodsIntro: String,
        var mainPicUrl: String,
        var infoUrl: String,
        var showPrice: String,
        var salesPrice: String,
        var goodsShow: Int,
        var goodsClick: Long,
        var goodsState: Int,
        var goodsCommend: Int,
        var createTime: Long,
        // var commentNum: Long,
        var saleNum: Long,
        var storeNum: Long,
        var goodsSlide: List<Image>?,
        var comments: Array<CommentData?>?,
        var totalCommentsNum: Int,
        var goodCommentsNum: Int,
        var normalCommentsNum: Int,
        var badCommentsNum: Int,
        var picCommentsNum: Int,
        var specUrl: String
        //        var skuAttrs: Array<SkuAttrsData>,
        //        var skuValues: Array<SkuInfoData>
){
    companion object{
        fun of(source: Goods, slide: List<Image>?, comments: Array<CommentData?>?,
               totalCommentsNum: Int, goodCommentsNum: Int, normalCommentsNum: Int,
               badCommentsNum: Int, picCommentsNum: Int) =
                GoodsData(source.goodsId, source.productId,
                source.shopId,source.goodsName,source.goodsIntro,source.mainPicUrl,
                        source.infoUrl, source.showPrice, source.salesPrice, source.goodsShow,
                        source.goodsClick, source.goodsState, source.goodsCommend,
                        source.createTime, source.saleNum, source.storeNum,
                        slide, comments, totalCommentsNum, goodCommentsNum, normalCommentsNum,
                        badCommentsNum, picCommentsNum, source.specUrl)
    }

    var createTimeShow: String = ""
        get() = createTime.dateFormatLongToString()
    var showPriceShow: String = ""
        get() = showPrice
    var salesPriceShow: String = ""
        get() = salesPrice
}