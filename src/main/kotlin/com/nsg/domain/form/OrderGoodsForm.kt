package com.nsg.domain.form

/**
 * Created by lijc on 16/3/8.
 */

class OrderGoodsForm {
    var goodsId: Long? = null
    var productId: Long? = null
    var skuId: Long? = null
    var shopId: Long? = null
    var goodsName: String? = null
    var goodsIntro: String? = null
    var mainPicUrl: String? = null
    var introUrl: String? = null
    var showPrice: Int? = null
    var salesPrice: Int? = null
    var goodsShow: Int? = null
    var goodsClick: Long? = null
    var goodsState: Int? = null
    var productCommend: Int? = null
    var createTime: String? = null
    // var commentNum: Long,
    var saleNum: Long? = null
    var storeNum: Long? = null
    var skuAttrs: Array<SkuAttrForm>? = null
}