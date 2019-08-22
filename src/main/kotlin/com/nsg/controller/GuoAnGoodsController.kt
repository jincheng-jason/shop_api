package com.nsg.controller

import com.nsg.domain.Comment
import com.nsg.domain.CommentType
import com.nsg.domain.Goods
import com.nsg.domain.data.CommentData
import com.nsg.domain.data.GoodsData
import com.nsg.domain.data.SkuInfoData
import com.nsg.domain.form.ShopIds
import com.nsg.mapper.GoodsMapper
import com.nsg.service.CommentService
import com.nsg.service.GoodsService
import com.nsg.service.ImageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import javax.servlet.http.HttpServletResponse

/**
 * Created by lijc on 16/2/21.
 */

@Api(basePath = "/v1/mall/guoan", value = "guoangoods", description = "国安商品API", produces = "application/json", position = 5)
@RestController
@RequestMapping("/v1/mall/guoan")
class GuoAnGoodsController
@Autowired constructor(
        var goodsService: GoodsService,
        var commentService: CommentService,
        var imageService: ImageService,
        var goodsMapper: GoodsMapper
) {

    val shopId = ShopIds.GUOAN.shopId
    private val LOGGER = LoggerFactory.getLogger(GoodsController::class.java)

    @ApiOperation(httpMethod = "GET", value = "商城首页", response = Array<Goods>::class)
    @RequestMapping(value = "/goods", method = arrayOf(RequestMethod.GET))
    fun getGoods(
            @RequestParam(value = "timestamp", required = false) timestamp: Long?,
            response: HttpServletResponse
    ): List<Goods> {

        try {
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return goodsService.getCommendGoodsList(mapOf<String, Any?>("timestamp" to timestamp, "shopId" to shopId))
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return listOf()
        }
    }

    @ApiOperation(httpMethod = "GET", value = "获取商品信息", response = GoodsData::class)
    @RequestMapping(value = "/goods/{goods_id}", method = arrayOf(RequestMethod.GET))
    fun getGoodsById(
            @PathVariable goods_id: Long,
            response: HttpServletResponse
    ): GoodsData {

        try {
            var goods = goodsService.getById(goods_id)
            if (goods == null) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("无此商品", "UTF-8"))
                return GoodsData(0, 0, 0, "", "", "", "", "", "", 0, 0, 0, 0, 0, 0, 0, null, null, 0, 0, 0, 0, 0, "")
            }

            var goodsImgs = goodsService.getGoodsImgs(goods_id)

            var map = mapOf("goodsId" to goods_id, "timestamp" to null, "limit" to 5)
            var comments = commentService.getCommentsByGoods(map)
            var commentCounts: List<Int>? = commentService.getGoodsCommentCounts(goods_id)

            var totalCommentsNum = when {
                (commentCounts == null || commentCounts.isEmpty()) -> 0
                else -> commentCounts[0]
            }
            var goodCommentsNum = when {
                (commentCounts == null || commentCounts.size < 2) -> 0
                else -> commentCounts[1]
            }
            var normalCommentsNum = when {
                (commentCounts == null || commentCounts.size < 3) -> 0
                else -> commentCounts[2]
            }
            var badCommentsNum = when {
                (commentCounts == null || commentCounts.size < 4) -> 0
                else -> commentCounts[3]
            }
            var picCommentsNum = when {
                (commentCounts == null || commentCounts.size < 5) -> 0
                else -> commentCounts[4]
            }
            var specUrl = ""


            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return GoodsData.of(goods, goodsImgs, CommentData.array(comments),
                    totalCommentsNum, goodCommentsNum, normalCommentsNum, badCommentsNum, picCommentsNum)
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return GoodsData(0, 0, 0, "", "", "", "", "", "", 0, 0, 0, 0, 0, 0, 0, null, null, 0, 0, 0, 0, 0, "")
        }
    }

    @ApiOperation(httpMethod = "GET", value = "获取商品的SKU属性集", response = GoodsData::class)
    @RequestMapping(value = "/goods/{goods_id}/attrs", method = arrayOf(RequestMethod.GET))
    fun getGoodsAttrByGoodsId(
            @PathVariable goods_id: Long,
            response: HttpServletResponse
    ): List<Map<String, Any>> {

        try {
            var map = emptyMap<String, Any>()

            val attrs = goodsService.getGoodsAttrs(goods_id)
            var attrList = emptyList<Map<String, Any>>()
            if (attrs != null && attrs.isNotEmpty()) {
                var attrNames: Set<String> = emptySet()
                attrs.forEach { attrNames = attrNames.plus(it.attrName) }
                for (name in attrNames) {
                    var valueList = attrs.filter { it.attrName.equals(name) }.toList()
                    map = map.plus("attr_name" to name).plus("attr_value" to valueList)
                    attrList = attrList.plus(map)
                }
            }

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return attrList
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return listOf()
        }
    }

    @ApiOperation(httpMethod = "GET", value = "获取商品的SKU属性集", response = GoodsData::class)
    @RequestMapping(value = "/goods/{goods_id}/skus", method = arrayOf(RequestMethod.GET))
    fun getGoodsSkusByGoodsId(
            @PathVariable goods_id: Long,
            response: HttpServletResponse
    ): List<SkuInfoData> {

        try {
            var skuInfos = goodsService.getSkuInfos(goods_id);

            var skuInfoList: List<SkuInfoData> = emptyList()
            skuInfos.forEach {
                skuInfoList = skuInfoList.plus(SkuInfoData.of(it, goods_id))
            }
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return skuInfoList
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return listOf()
        }
    }

    @ApiOperation(httpMethod = "GET", value = "商品库存", response = Map::class)
    @RequestMapping(value = "/goods/{goods_id}/stocks", method = arrayOf(RequestMethod.GET))
    fun getGoodsStocks(
            @PathVariable goods_id: Long,
            response: HttpServletResponse
    ): Map<String, Any> {

        try {
            var stock = goodsService.getGoodsStock(goods_id)
            var result: MutableMap<String, Any> = hashMapOf("stock" to stock)
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return result
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return mapOf()
        }
    }

    @ApiOperation(httpMethod = "GET", value = "商品评论列表", response = Array<CommentData>::class)
    @RequestMapping(value = "/goods/{goods_id}/comments", method = arrayOf(RequestMethod.GET))
    fun getGoodsComments(
            @PathVariable goods_id: Long,
            @RequestParam(value = "type", required = false) type: String?, //{all,good,normal,bad,pic}
            @RequestParam(value = "timestamp", required = false) timestamp: Long?,
            response: HttpServletResponse
    ): List<Comment> {

        try {
            var limit = 10
            var map = mapOf("goodsId" to goods_id, "timestamp" to timestamp, "limit" to limit)
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            when (type) {
                CommentType.GOOD.type -> return commentService.getGoodCommentsByGoods(map)!!
                CommentType.NORMAL.type -> return commentService.getNormalCommentsByGoods(map)!!
                CommentType.BAD.type -> return commentService.getBadCommentsByGoods(map)!!
                CommentType.PIC.type -> return commentService.getPicCommentsByGoods(map)!!
                else -> return commentService.getCommentsByGoods(map)
            }
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return emptyList()
        }
    }


    @ApiOperation(httpMethod = "GET", value = "获取一条评论", response = CommentData::class)
    @RequestMapping(value = "/goods/{goods_id}/comments/{comment_id}", method = arrayOf(RequestMethod.GET))
    fun getOneComment(@PathVariable goods_id: Long,
                      @PathVariable comment_id: Long,
                      response: HttpServletResponse
    ): Comment? {

        try {
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            var comment = commentService.getById(comment_id)
            return if (comment == null) Comment(0, 0, 0, 0, 0, 0, "", 0, 0, "", "", "", null, 0) else comment
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return Comment(0, 0, 0, 0, 0, 0, "", 0, 0, "", "", "", null, 0)
        }

    }

    @ApiOperation(httpMethod = "DELETE", value = "删除一条评论")
    @RequestMapping(value = "/goods/{goods_id}/comments/{comment_id}", method = arrayOf(RequestMethod.DELETE))
    fun deleteOneComment(@PathVariable goods_id: Long,
                         @PathVariable comment_id: Long,
                         response: HttpServletResponse): Map<Any, Any> {

        try {
            commentService.deleteComment(comment_id)
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("删除成功", "UTF-8"))
            return mapOf()
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("删除失败", "UTF-8"))
            return mapOf()
        }
    }

    //    @ApiOperation(httpMethod = "GET", value = "添加所有虚拟卡", response = Map::class)
    //    @RequestMapping(value = "/goods/new_props", method = arrayOf(RequestMethod.GET))
    //    fun newProps(response: HttpServletResponse
    //    ): Map<Any,Any>? {
    //
    //        val url = "http://123.59.84.71:8888/user/v1/items/add"
    //        val json = "{\"itemTypeKey\":\"BadgeCardGuoAn\",\"userId\":3}"
    //        val response = Requests.post(url).data(json).addHeader("Content-Type", "application/json").verify(false).text()
    //        LOGGER.info(response.getBody())
    //        val resultMap = JSON.parse(response.getBody()) as Map<String, Any>
    //        val operCode = resultMap["oper_code"] as String
    //        if (operCode.equals("1")) {
    //            //绑定成功
    //            LOGGER.info("--------绑定虚拟物品成功:" + json)
    //            //将orderitem状态置为unrate
    ////            val typeMap = HashMap<String, Any>()
    ////            typeMap.put("itemType", OrderItemType.UNRATE.type)
    ////            typeMap.put("orderItemId", orderItem.orderItemId)
    ////            orderItemMapper.updateItemType(typeMap)
    //        }
    //
    //        return mapOf()
    //
    ////        try {
    ////            response.status = HttpServletResponse.SC_OK
    ////            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
    ////
    ////            //新建product
    ////            var product = Products(0, "123456", "徽章卡", Date().time, 99)
    ////            goodsMapper.saveProduct(product)
    ////
    ////            //新建goods
    ////            var goods = Goods(0,product.productId,ShopIds.CSL.shopId,1,"彰显身份，这就是我的主队","彰显身份，这就是我的主队",
    ////                    "http://7xldo6.com2.z0.glb.qiniucdn.com/CSL3.7.JPG","",10,1,1,10,1,1,Date().time,1,90,null,true)
    ////
    ////            goodsMapper.saveGoods(goods)
    ////
    ////            var img = Image(0,"","","http://7xldo6.com2.z0.glb.qiniucdn.com/CSL3.7.JPG",null,null)
    ////            imageService.save(img)
    ////
    ////            var goods_ref_img = GoodsRefImg(0, goods.goodsId, img.imgId)
    ////            goodsMapper.saveGoodsRefImg(goods_ref_img)
    ////
    ////            for(propsItem in PropsItemType.values()){
    ////
    ////
    ////                //新建product attr
    ////                var attr = SkuAttr(0,"俱乐部",propsItem.desciption)
    ////                var attr1 = SkuAttr(0,"天数",propsItem.effectTime)
    ////                goodsMapper.saveAttr(attr)
    ////                goodsMapper.saveAttr(attr1)
    ////
    ////
    ////                //新建product sku
    ////                var sku = ProductSku(0, product.productId, 1, 1, 99, propsItem.imgUrl, 9, 1)
    ////                goodsMapper.saveSku(sku)
    ////
    ////                //新建product ref sku
    ////                var product_ref_sku = ProductRefSku(0,product.productId,sku.productSkuId)
    ////                goodsMapper.saveProductRefSku(product_ref_sku)
    ////
    ////
    ////                //新建product ref attr
    ////                var product_ref_attr = ProductRefAttr(0,sku.productSkuId,attr.skuAttrId)
    ////                var product_ref_attr1 = ProductRefAttr(0,sku.productSkuId,attr1.skuAttrId)
    ////                goodsMapper.saveProductRefAttr(product_ref_attr)
    ////                goodsMapper.saveProductRefAttr(product_ref_attr1)
    ////
    ////
    ////                //新建props
    ////                var props = Props(0,propsItem.type,1,product.productId,sku.productSkuId,Date().time)
    ////                goodsMapper.saveProps(props)
    ////
    ////            }
    ////
    ////            return mapOf()
    ////        } catch(e: Exception) {
    ////            e.printStackTrace()
    ////            response.status = HttpServletResponse.SC_BAD_REQUEST
    ////            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
    ////            return mapOf()
    ////        }
    //
    //    }

    //    @ApiOperation(httpMethod = "PUT", value = "修改一条评论")
    //    @RequestMapping(value = "/goods/{goods_id}/comments/{comment_id}", method = arrayOf(RequestMethod.PUT))
    //    fun editOneComment(@PathVariable goods_id: Long,
    //                       @PathVariable comment_id: Long,
    //                       @RequestBody(required = true) commentForm: CommentForm,
    //                       response: HttpServletResponse) : Comment {
    //
    //        try {
    //
    //            response.status = HttpServletResponse.SC_OK
    //            response.addHeader("X-Err-Message", URLEncoder.encode("更新成功", "UTF-8"))
    //            return mapOf()
    //        }catch(e: Exception){
    //
    //        }
    //    }

}