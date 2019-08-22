package com.nsg.mapper

import com.nsg.domain.*

/**
 * Created by lijc on 16/3/9.
 */

interface GoodsMapper {

    fun getCommendGoodsList(map: Map<String, Any?>): List<Goods>

    fun getById(goodsId: Long): Goods

    fun getGoodsImgs(goodsId: Long): List<Image>

    fun getGoodsStock(goodsId: Long): Int?

    fun saveProduct(product: Products)

    fun saveAttr(attr: SkuAttr)

    fun saveSku(sku: ProductSku)

    fun saveProductRefSku(refSku: ProductRefSku)

    fun saveProductRefAttr(refAttr: ProductRefAttr)

    fun saveProps(props: Props)

    fun saveGoods(goods: Goods)

    fun saveGoodsRefImg(goodsRefImg: GoodsRefImg)

    fun updateGoodsPrice(map: Map<String, Any?>)

}