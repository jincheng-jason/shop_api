package com.nsg.controller

import com.alibaba.fastjson.JSON
import com.nsg.domain.Cart
import com.nsg.domain.CartItem
import com.nsg.domain.ProvinceEnum
import com.nsg.domain.data.CartItemData
import com.nsg.domain.data.ShippingData
import com.nsg.domain.form.CartItemForm
import com.nsg.domain.form.ShippingFeeForm
import com.nsg.domain.form.ShopIds
import com.nsg.service.AddressService
import com.nsg.service.CartService
import com.nsg.service.GoodsService
import com.nsg.service.OrderService
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

@Api(basePath = "/v1/mall/csl", value = "cart", description = "购物车API", produces = "application/json", position = 4)
@RestController
@RequestMapping("/v1/mall/csl")
class CartController
@Autowired constructor(
        val cartService: CartService,
        val orderService: OrderService,
        val goodsService: GoodsService,
        val addressService: AddressService) {

    val shopId = ShopIds.CSL.shopId
    private val LOGGER = LoggerFactory.getLogger(CartController::class.java)

    @ApiOperation(httpMethod = "GET", value = "获取用户的购物车信息", response = CartItemData::class, responseContainer = "Array")
    @RequestMapping(value = "/carts", method = arrayOf(RequestMethod.GET))
    fun getCartByUser(@RequestParam(value = "user_id", required = true) user_id: Long,
                      response: HttpServletResponse): Cart {

        try {

            var map = mapOf("userId" to user_id, "shopId" to shopId)
            var cart = cartService.getCartByUser(map)
            if (cart == null) {
                cart = Cart(0, user_id, shopId, null, null)
                cartService.newCartForUser(cart)
            }
            var cartItems = cart.cartItems
            if (cartItems != null && cartItems.isNotEmpty()) {
                if (cartItems[0].cartItemId == 0L) {
                    cart.cartItems = null
                }
            }
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return cart
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return Cart(0, 0, 0, 0, null)
        }

    }

    @ApiOperation(httpMethod = "POST", value = "将一件商品加入购物车,返回购物车中商品数量")
    @RequestMapping(value = "/carts", method = arrayOf(RequestMethod.POST))
    fun putInCart(@RequestBody(required = true) cartItemForm: CartItemForm,
                  response: HttpServletResponse): Map<Any, Any> {
        try {
            //            var cartId = cartItemForm.cartId

            LOGGER.info("----------请求参数:${JSON.toJSON(cartItemForm)}")

            var skuId = cartItemForm.skuId
            var buyNum = cartItemForm.buyNum
            var userId = cartItemForm.userId
            var goodsId = cartItemForm.goodsId

            if (skuId.equals(null) || skuId.equals(0L) || buyNum.equals(null) || buyNum.equals(0) ||
                    userId == null || userId.equals(0L) || goodsId.equals(null) || goodsId.equals(0L)) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("请求参数错误", "UTF-8"))
                return mapOf()
            }

            //            var goods = cartItemForm.goods

            //查询该用户有无购物车,无则创建
            var map = mapOf("userId" to userId, "shopId" to shopId)
            var cart = cartService.getCartByUser(map)

            if (cart == null) {
                cart = Cart(0, userId, shopId, null, null)
                cartService.newCartForUser(cart)
            }
            var cartId = cart.cartId

            //检查SKU是否有优惠或限制
            if (orderService.isSkuInPromotion(skuId)) {

                //7天徽章卡限购1张
                if (buyNum > 1) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode("此商品限购1件", "UTF-8"))
                    return mapOf()
                }

                //查询此用户是否购买过7天徽章卡
                if (orderService.isUserSkuInPromotion(mapOf("userId" to userId, "skuId" to skuId))) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode("已购买过此限购商品", "UTF-8"))
                    return mapOf()
                }

            }

            //首先校验是否有货
            //查询sku的库存数量
            //            var storeNum = orderService.getSkuStore(skuId)

            var skuInfo = orderService.getSkuById(skuId)

            if (skuInfo == null || skuInfo.skuId.equals(0L)) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("无此商品SKU信息", "UTF-8"))
                return mapOf()
            }

            if (skuInfo.state == false) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("此商品已失效", "UTF-8"))
                return mapOf()
            }

            if (skuInfo.storeNum < buyNum) {
                //库存不足,则返回失败,并返回库存不足商品信息
                //和前端定具体返回值,以确定具体库存不足的cartitem
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("此商品库存不足", "UTF-8"))
                response.addHeader("X-Err-Code", "2000")
                return mapOf()
            }


            //查询此购物车中有无此skuid的商品,如有则增加buynum,无则新增记录
            var cartItem = cartService.getCartItemByIdAndSku(mapOf("cartId" to cartId, "skuId" to skuId))
            if (cartItem == null || cartItem.cartItemId.equals(0)) {
                cartItem = CartItem(0, cartId, skuId, buyNum, goodsId, null, 0, false)
                cartService.putInCart(cartItem)
            } else {
                cartService.increaseBuyNum(mapOf("cartItemId" to cartItem.cartItemId, "buyNum" to buyNum))
                cartItem.buyNum += buyNum
            }

            var cartCount = cartService.cartCount(cartId)

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            return mapOf("badge" to cartCount)
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return mapOf()
        }

    }

    @ApiOperation(httpMethod = "DELETE", value = "清空或移出购物车,带请求体为移出")
    @RequestMapping(value = "/carts/{cart_id}", method = arrayOf(RequestMethod.DELETE))
    fun emptyCart(@PathVariable cart_id: Long,
                  @RequestBody(required = false) cartItemIds: List<Long>?,
                  response: HttpServletResponse) : Map<Any,Any> {

        try {
            LOGGER.info("----------请求参数:${JSON.toJSON(cartItemIds)}")
            if (cartItemIds != null && cartItemIds.isNotEmpty()) {
                cartService.batchDeleteCartItems(cartItemIds)
            } else {
                cartService.clearCart(cart_id)
            }

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            return mapOf()
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return mapOf()
        }

    }

    @ApiOperation(httpMethod = "PUT", value = "更新购物车购买数量")
    @RequestMapping(value = "/carts/{cart_id}", method = arrayOf(RequestMethod.PUT))
    fun editCart(@PathVariable cart_id: Long,
                 @RequestBody(required = true) cartItemForms: List<CartItemForm>,
                 response: HttpServletResponse): Map<Any, Any> {

        try {
            for (cartItem in cartItemForms) {
                cartService.updateCartItemBuyNum(cartItem)
            }

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("更新成功", "UTF-8"))
            return mapOf()
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("更新失败", "UTF-8"))
            return mapOf()
        }
    }

    @ApiOperation(httpMethod = "POST", value = "根据商品id和订购数量,获得运费", response = ShippingData::class)
    @RequestMapping(value = "/shipping-fee", method = arrayOf(RequestMethod.POST))
    fun getShippingFee(@RequestBody(required = true) shippingFeeForm: ShippingFeeForm,
                       response: HttpServletResponse): ShippingData {

        //通过传入的goods_id,从SKU表中得到此sku的重量,以重量和订购数量算出所有运费
        //判断是否虚拟物品,虚拟物品则邮费为0
        LOGGER.info("-------获取邮费:${JSON.toJSONString(shippingFeeForm)}")

        try {
            var shippingFee = 0
            var totalWeight = 0 //总重量,单位g

            val addressId = shippingFeeForm.addressId
            val address = addressService.getById(addressId)

            val cartItemForms = shippingFeeForm.cartItems

            for (cartItem in cartItemForms) {
                var goodsId = cartItem.goodsId
                if (goodsId.equals(null) || goodsId.equals(0L)) {
                    goodsId = cartItem.goods!!.goodsId
                }
                val goods = goodsService.getById(goodsId)
                val buyNum = cartItem.buyNum
                if (!goods.isVirtual) {
                    //如果是实体商品,则按照规则计算邮费
                    val skuId = cartItem.skuId
                    val sku = orderService.getSkuById(skuId)

                    if (sku.weight == null) {
                        response.status = HttpServletResponse.SC_BAD_REQUEST
                        response.addHeader("X-Err-Message", URLEncoder.encode(goods.goodsName + ":查询商品重量失败", "UTF-8"))
                        return ShippingData(0)
                    }
                    totalWeight += (sku.weight * buyNum)

                }
            }

            //按照订单商品总重量和邮寄地址算出总邮费

            val province = address.province
            val kilogram = 1000
            val yuan = 100
            if (province.contains(ProvinceEnum.BEIJING.province, false)) {
                //如果同城,首重(1公斤)6元,续重(1公斤)1元
                shippingFee = 6 * yuan
                if (totalWeight > kilogram) {
                    //余重每1公斤加1元
                    shippingFee += (((totalWeight - kilogram) / kilogram) + 1) * yuan
                }

            } else if (province.contains(ProvinceEnum.TIANJIN.province, false)
                    || province.contains(ProvinceEnum.HEBEI.province, false)
                    || province.contains(ProvinceEnum.SHANDONG.province, false)
                    || province.contains(ProvinceEnum.SHANXI.province, false)
                    || province.contains(ProvinceEnum.HENAN.province, false)
            ) {
                //区域 天津,河北,山东,山西,河南,首重 7元,续重 2元
                shippingFee = 7 * yuan
                if (totalWeight > kilogram) {
                    //余重每公斤加2元
                    shippingFee += (((totalWeight - kilogram) / kilogram) + 1) * 2 * yuan
                }

            } else if (province.contains(ProvinceEnum.XINJIANG.province, false)
                    || province.contains(ProvinceEnum.XIZANG.province, false)
                    || province.contains(ProvinceEnum.QINGHAI.province, false)
                    || province.contains(ProvinceEnum.GANSU.province, false)
                    || province.contains(ProvinceEnum.YUNNAN.province, false)
                    || province.contains(ProvinceEnum.GUIZHOU.province, false)
            ) {

                //偏远地区 新疆,西藏,青海,甘肃,云南,贵州,首重 15元, 续重 9元
                shippingFee = 15 * yuan
                if (totalWeight > kilogram) {
                    //余重每公斤加9元
                    shippingFee += (((totalWeight - kilogram) / kilogram) + 1) * 9 * yuan
                }

            } else {
                //其他地区 首重 8元,续重 5元
                shippingFee = 8 * yuan
                if (totalWeight > kilogram) {
                    //余重每公斤加5元
                    shippingFee += (((totalWeight - kilogram) / kilogram) + 1) * 5 * yuan
                }

            }

            LOGGER.info("-------此单邮费(分):$shippingFee")

            return ShippingData(shippingFee)
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return ShippingData(0)
        }
    }

    @ApiOperation(httpMethod = "GET", value = "获取用户的购物车的角标数值", response = Map::class)
    @RequestMapping(value = "/carts/badge", method = arrayOf(RequestMethod.GET))
    fun getCartBadge(@RequestParam(value = "user_id", required = true) user_id: Long,
                     response: HttpServletResponse): Map<Any, Any> {

        try {
            var cartCount = cartService.cartCountOfUser(mapOf("userId" to user_id, "shopId" to shopId))
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return mapOf("badge" to cartCount)
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return mapOf()
        }
    }

    @ApiOperation(httpMethod = "POST", value = "校验用户当前购物车中的商品是否库存充足", response = Map::class)
    @RequestMapping(value = "/carts/check_stock", method = arrayOf(RequestMethod.POST))
    fun checkStock(@RequestBody(required = true) cartItemForms: List<CartItemForm>,
                   response: HttpServletResponse): Map<Any, Any> {

        try {
            for (cartItem in cartItemForms) {

                var skuId = cartItem.skuId
                var buyNum = cartItem.buyNum
                var goodsName = cartItem.goods?.goodsName

                //查询sku的库存数量
                var skuInfo = orderService.getSkuById(skuId)

                if (skuInfo == null || skuInfo.skuId.equals(0L)) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode(goodsName + "无此商品SKU信息", "UTF-8"))
                    return mapOf()
                }

                if (skuInfo.state == false) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode(goodsName + "此商品已失效", "UTF-8"))
                    return mapOf()
                }

                if (skuInfo.storeNum < buyNum) {
                    //库存不足,则返回失败,并返回库存不足商品信息
                    //和前端定具体返回值,以确定具体库存不足的cartitem
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode(goodsName + "商品库存不足", "UTF-8"))
                    response.addHeader("X-Err-Code", "2000")
                    return mapOf()
                }

            }
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return mapOf()
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return mapOf()
        }
    }





}