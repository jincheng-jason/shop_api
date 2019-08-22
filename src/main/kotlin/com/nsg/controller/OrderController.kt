package com.nsg.controller

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.nsg.domain.*
import com.nsg.domain.data.OrderData
import com.nsg.domain.form.*
import com.nsg.env.UrlEnum
import com.nsg.service.AddressService
import com.nsg.service.CommentService
import com.nsg.service.ImageService
import com.nsg.service.OrderService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.dongliu.requests.Requests
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.servlet.http.HttpServletResponse

/**
 * Created by lijc on 16/2/25.12
 */

@Api(basePath = "/v1/mall/csl", value = "order", description = "订单API", produces = "application/json", position = 8)
@RestController
@RequestMapping("/v1/mall/csl")
class OrderController
@Autowired constructor(var orderService: OrderService,
                       var commentService: CommentService,
                       var imageService: ImageService,
                       var addressService: AddressService) {

    val shopId = ShopIds.CSL.shopId
    private val LOGGER = LoggerFactory.getLogger(OrderController::class.java)

    @ApiOperation(httpMethod = "GET", value = "获取订单列表", response = Array<OrderData>::class)
    @RequestMapping(value = "/orders", method = arrayOf(RequestMethod.GET))
    fun getOrdersByUserAndType(
            @RequestParam(value = "user_id", required = true) user_id: Long,
            @RequestParam(value = "type", required = false) type: String?,
            @RequestParam(value = "timestamp", required = false) timestamp: Long?,
            response: HttpServletResponse
    ): List<Order> {
        try {
            //type={unpay,unship,unrecv(未收货),unrate,rated,dealclose} 默认 unpay

            val realType: String?
            if (type.equals(OrderType.ALL.type) || type == null) {
                realType = null
            } else {
                realType = type
            }
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return orderService.findAll(mapOf("user_id" to user_id, "type" to realType, "timestamp" to timestamp, "shopId" to shopId))
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return emptyList()
        }

    }

    @ApiOperation(httpMethod = "POST", value = "获取最新的cartitem的信息,最新的sku价格", response = Array<OrderData>::class)
    @RequestMapping(value = "/orders/get_latest_cart_items", method = arrayOf(RequestMethod.POST))
    fun getLatestCartItems(
            @RequestBody(required = true) cartItemForms: List<CartItemForm>,
            response: HttpServletResponse
    ): List<CartItem> {
        try {
            var cartItems: List<CartItem> = emptyList()
            for (cartItem in cartItemForms) {

                var skuId = cartItem.skuId
                var buyNum = cartItem.buyNum
                var goodsName = cartItem.goods?.goodsName

                //查询sku的库存数量
                var skuInfo = orderService.getSkuById(skuId)

                if (skuInfo == null || skuInfo.skuId.equals(0L)) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode(goodsName + "无此商品SKU信息", "UTF-8"))
                    return emptyList()
                }

                if (skuInfo.storeNum < buyNum) {
                    //库存不足,则返回失败,并返回库存不足商品信息
                    //和前端定具体返回值,以确定具体库存不足的cartitem
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode(goodsName + "商品库存不足", "UTF-8"))
                    response.addHeader("X-Err-Code", "2000")
                    return emptyList()
                }

                var updateCartItem = CartItem(cartItem.cartItemId, 0, skuId, buyNum, cartItem.goods!!.goodsId, cartItem.goods, skuInfo.salesPrice, skuInfo.state)
                cartItems = cartItems.plus(updateCartItem)
            }
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return cartItems
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return emptyList()
        }
    }

    @ApiOperation(httpMethod = "POST", value = "添加订单信息")
    @RequestMapping(value = "/orders", method = arrayOf(RequestMethod.POST))
    fun addOrder(
            @RequestParam(value = "user_id", required = true) user_id: Long,
            @RequestBody(required = true) orderForm: OrderForm,
            response: HttpServletResponse): Order {

        try {

            var addressId = orderForm.addressId
            if (addressId.equals(null) || addressId.equals(0L)) {
                if (orderForm.address != null) {
                    addressId = orderForm.address!!.addressId
                    orderForm.addressId = addressId
                }
            }

            var userId = user_id
            orderForm.userId = user_id
            var totalPrice = orderForm.totalPrice

            var cartItems = orderForm.cartItems

            if (userId.equals(null) || userId.equals(0L) ||
                    totalPrice.equals(null) || totalPrice.equals(0) || cartItems.equals(null) || cartItems.size.equals(0)) {
                LOGGER.info("-----------请求参数错误:${JSON.toJSON(orderForm)}")
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("请求参数错误", "UTF-8"))
                return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, null, 0, "", "", 0, false)
            }

            LOGGER.info("-----------请求参数:${JSON.toJSON(orderForm)}")

            var realTotalPrice = 0
            var orderItemList: List<OrderItem> = emptyList()
            //首先校验是否有货,并校验商品总价是否正确
            for (cartItem in cartItems) {

                var skuId = cartItem.skuId
                var buyNum = cartItem.buyNum
                var goodsName = cartItem.goods?.goodsName

                //检查SKU是否有优惠或限制
                if (orderService.isSkuInPromotion(skuId)) {

                    //7天徽章卡限购1张
                    if (buyNum > 1) {
                        response.status = HttpServletResponse.SC_BAD_REQUEST
                        response.addHeader("X-Err-Message", URLEncoder.encode("此商品限购1件", "UTF-8"))
                        return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, "此商品限购1件", 0, "", "", 0, false)
                    }

                    //查询此用户是否购买过7天徽章卡
                    if (orderService.isUserSkuInPromotion(mapOf("userId" to userId, "skuId" to skuId))) {
                        response.status = HttpServletResponse.SC_BAD_REQUEST
                        response.addHeader("X-Err-Message", URLEncoder.encode("已购买过此限购商品", "UTF-8"))
                        return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, "已购买过此限购商品", 0, "", "", 0, false)
                    }

                }

                //查询sku的库存数量
                var skuInfo = orderService.getSkuById(skuId)

                if (skuInfo == null || skuInfo.skuId.equals(0L)) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode(goodsName + "无此商品SKU信息", "UTF-8"))
                    return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, "无此商品SKU信息", 0, "", "", 0, false)
                }

                if (skuInfo.state == false) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode(goodsName + "此商品已失效", "UTF-8"))
                    return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, "此商品已失效", 0, "", "", 0, false)
                }

                if (skuInfo.storeNum < buyNum) {
                    //库存不足,则返回失败,并返回库存不足商品信息
                    //和前端定具体返回值,以确定具体库存不足的cartitem
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode(goodsName + "商品库存不足", "UTF-8"))
                    response.addHeader("X-Err-Code", "2000")
                    return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, "库存不足", 0, "", "", 0, false)
                }

                var orderItem = OrderItem(0, 0, skuInfo.skuId, cartItem.goods, buyNum, cartItem.goodsId, goodsName!!, skuInfo.salesPrice, 0, "", false)

                orderItemList = orderItemList.plus(orderItem)
                realTotalPrice += (skuInfo.salesPrice * buyNum)
            }

            realTotalPrice += orderForm.postage

            LOGGER.info("实际订单总价:$realTotalPrice,提交订单总价:$totalPrice")

            if (!totalPrice.equals(realTotalPrice)) {
                //如果订单总价与实际不符,则返回失败,errcode 3000
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("订单总价有误", "UTF-8"))
                response.addHeader("X-Err-Code", "3000")
                return Order(0, "", 0, 0, 0, 0, "", realTotalPrice - orderForm.postage, orderForm.postage, realTotalPrice, orderForm.address, orderItemList, "订单总价有误", 0, "", "", 0, false)
            }

            var description = ""
            var goodsName = ""
            if (cartItems.size > 0) {
                goodsName = cartItems[0].goods!!.goodsName
            }

            description += goodsName + "等" + cartItems.size + "件商品"

            orderForm.description = description

            val now = LocalDateTime.now()
            val zoneId = ZoneId.systemDefault()
            val date = now.atZone(zoneId).toInstant().toEpochMilli()
            //            过期时间设置为订单产生时间过5分钟
            //            线上过期时间设置为订单产生时间过1天
            //            val overdueTime = now.plusMinutes(5).atZone(zoneId).toInstant().toEpochMilli()
            val overdueTime = now.plusDays(1).atZone(zoneId).toInstant().toEpochMilli()

            val random: Int = Random().nextInt(900) + 100
            val orderSN = "CSL" + date + random
            orderForm.orderSN = orderSN
            orderForm.overdueTime = overdueTime
            orderForm.shopId = shopId

            //通过接口拿到此用户的uuid
            //            val userUrl = UrlEnum.USER_INFO_URL.url + user_id
            //            LOGGER.info(userUrl)
            //            val userResponse = Requests.get(userUrl).verify(false).text()
            //            LOGGER.info("获取用户uuid:${userResponse.body}")
            //            val userMap = JSON.parse(userResponse.body) as Map<*, *>
            val userMap = orderService.getUserInfo(user_id)
            val uuid = (userMap["data"] as Map<*, *>)["nhId"]

            if (uuid == null) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("获取用户信息失败", "UTF-8"))
                response.addHeader("X-Err-Code", "2001")
                return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, "获取用户信息失败", 0, "", "", 0, false)
            }

            orderForm.uuidUserId = uuid as String

            //将订单信息提交给支付中心
            var url = "${UrlEnum.PAYMENT_CENTER_URL.url}/pay/unifiedorder?unionuserid=${uuid}&out_trade_no=${orderSN}&total_fee=${orderForm.totalPrice}&body=${description}&appid=1";
            LOGGER.info(url)
            var result = Requests.get(url).verify(false).text()
            LOGGER.info("-------订单信息提交支付中心:${result.body}")

            var order = orderService.addOrder(orderForm)

            //2016.9.7 确认,不需要自动设置默认地址
//            if (addressId != null && !addressId.equals(0L)) {
//                //查询此用户有没有默认地址,如果没有,则将此地址设为默认
//                val address = addressService.getDefault(userId)
//                if (address == null || address.addressId.equals(0L)) {
//                    LOGGER.info("此用户没有默认地址,将此次地址设为默认:${JSON.toJSONString(mapOf("userId" to userId, "addressId" to addressId))}")
//                    addressService.setAddressDefault(mapOf("userId" to userId, "addressId" to addressId))
//                }
//            }

            LOGGER.info("-------订单新增成功:${JSON.toJSON(order)}")
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            return order
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, null, 0, "", "", 0, false)
        }
    }

    @ApiOperation(httpMethod = "POST", value = "支付订单,请求支付中心统一下单接口")
    @RequestMapping(value = "/orders/{order_id}/pay", method = arrayOf(RequestMethod.POST))
    fun payOrder(@PathVariable order_id: Long,
                 @RequestBody(required = true) payForm: PayForm,
                 response: HttpServletResponse): Map<Any, Any> {

        try {

            LOGGER.info("----------支付订单请求参数:${JSON.toJSON(payForm)}")

            var orderSN = payForm.orderSN
            var totalPrice = payForm.totalPrice
            var body = payForm.description
            var type = payForm.type

            var url: String

            when (type) {
                PayType.WECHAT.type -> url = "${UrlEnum.PAYMENT_CENTER_URL.url}/wxpay/unifiedorder?out_trade_no=$orderSN&total_fee=$totalPrice&body=$body&period=1440&appid=1"
                PayType.ALIPAY.type -> url = "${UrlEnum.PAYMENT_CENTER_URL.url}/alipay/unifiedorder?out_trade_no=$orderSN&total_fee=$totalPrice&body=$body&period=1440&appid=1"
                else -> {
                    LOGGER.info("----------支付订单支付方式错误:$type")
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    response.addHeader("X-Err-Message", URLEncoder.encode("支付方式错误", "UTF-8"))
                    return mapOf()
                }
            }

            LOGGER.info(url)

            var result = Requests.get(url).verify(false).text()

            LOGGER.info(result.body)

            val map: Map<Any, Any> = JSON.parse(result.body) as Map<Any, Any>

            val code: Int = map["code"] as Int

            if (code != 0) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("处理失败", "UTF-8"))
                return map
            }


            //请求统一下单接口成功后,将此订单状态改为 checking , 2016.9.7 确认 取消确认中CHECKING状态
            val order = orderService.getOrderByOrderSN(orderSN)
            if (order == null || order.orderId.equals(0L)) {
                LOGGER.info("查询无此订单:$orderSN")
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("查无此订单", "UTF-8"))
                return mapOf()
            }

            //2016.9.7 确认 取消确认中CHECKING状态
            LOGGER.info("订单:$orderSN,存入支付渠道:$type")
            //并存入支付渠道
            orderService.changeOrderType(mapOf("orderId" to order.orderId, "type" to OrderType.UNPAY.type, "payType" to type))

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            return map
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return mapOf()
        }
    }

    @ApiOperation(httpMethod = "POST", value = "更新支付信息")
    @RequestMapping(value = "/orders/update_pay_status", method = arrayOf(RequestMethod.POST))
    fun updatePayStatus(@RequestBody(required = true) payStatusForm: PayStatusForm,
                        response: HttpServletResponse): Map<Any, Any> {

        try {

            LOGGER.info("接到更新支付信息请求参数:${JSON.toJSON(payStatusForm)}")

            var orderSN = payStatusForm.out_trade_no
            var paymentPrice = payStatusForm.real_fee

            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            var paymentTime = df.parse(payStatusForm.pay_time).time

            var paymentStatus = payStatusForm.pay_state
            var payType = payStatusForm.pay_type
            var date = Date().time

            if (paymentStatus == 1) {
                //支付成功

                var payment = Payment(0, shopId, paymentStatus, orderSN, paymentPrice, paymentTime, payType, date, null, null, null, null, null)

                //存入支付信息,更新order表,order状态置为unship
                orderService.savePayment(payment)
                LOGGER.info("更新支付信息请求成功${JSON.toJSONString(payment)}")
            } else if (paymentStatus == 0) {
                //支付失败,则查询订单支付状态,如果仍是失败,则回退订单状态
                LOGGER.info("支付中心返回支付失败:${JSON.toJSON(payStatusForm)}")
                val orderId = orderService.getOrderByOrderSN(orderSN).orderId
                orderService.getPayStatus(orderId, payType);
            }

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            return mapOf("result" to "Success")
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return mapOf("result" to "Failed")
        }
    }

    @ApiOperation(httpMethod = "GET", value = "查询支付信息")
    @RequestMapping(value = "/orders/{order_id}/get_pay_status", method = arrayOf(RequestMethod.GET))
    fun getPayStatus(@PathVariable order_id: Long,
                     @RequestParam(value = "pay_type", required = true) pay_type: String,
                     response: HttpServletResponse): Payment {
        try {

            LOGGER.info("查询支付信息:$pay_type,orderId:$order_id")
            //
            val payment = orderService.getPayStatus(order_id, pay_type)
            if (payment.paymentId.equals(-1L)) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("订单未支付", "UTF-8"))
                response.addHeader("X-Err-Code", payment.shopId.toString())
            } else if (payment.paymentId.equals(0L)) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode(payment.orderSN, "UTF-8"))
                response.addHeader("X-Err-Code", payment.shopId.toString())
            } else {
                response.status = HttpServletResponse.SC_OK
                response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            }

            return if (payment.paymentId > 0) payment else Payment(0, 0, 0, "", 0, 0, "", 0, null, null, null, null, null)
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return Payment(0, 0, 0, "", 0, 0, "", 0, null, null, null, null, null)
        }
    }

    //    @ApiOperation(httpMethod = "DELETE", value = "清空订单")
    //    @RequestMapping(value = "/orders", method = arrayOf(RequestMethod.DELETE))
    //    fun emptyOrders(@RequestParam(value = "user_id", required = true) user_id: Long,
    //                       response: HttpServletResponse) : Map<Any,Any> {
    //        //处理逻辑后补
    //
    //        response.status = HttpServletResponse.SC_OK
    //        response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
    //        return mapOf()
    //    }

    @ApiOperation(httpMethod = "GET", value = "获取订单信息", response = Order::class)
    @RequestMapping(value = "/orders/{order_id}", method = arrayOf(RequestMethod.GET))
    fun getOneOrder(@PathVariable order_id: Long,
                    response: HttpServletResponse): Order {
        try {
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return orderService.findAll(mapOf("order_id" to order_id)).first()
        } catch(e: Exception) {
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, null, 0, "", "", 0, false)
        }
    }

    @ApiOperation(httpMethod = "PUT", value = "取消订单")
    @RequestMapping(value = "/orders/{order_id}/cancel", method = arrayOf(RequestMethod.PUT))
    fun cancelOneOrder(@PathVariable order_id: Long,
                       response: HttpServletResponse): Order {
        try {
            //将订单状态改为 dealclose

            //验证订单当前状态,只有unpay状态可以取消
            val order = orderService.findAll(mapOf("order_id" to order_id)).first()

            if (!order.type.isNullOrEmpty() && order.type.equals(OrderType.UNPAY.type)) {
                orderService.changeOrderType(mapOf("orderId" to order_id, "type" to OrderType.DEALCLOSE.type))
                order.type = OrderType.DEALCLOSE.type
            } else {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("当前状态不可取消", "UTF-8"))
                return order
            }

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("取消成功", "UTF-8"))
            return order
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, null, 0, "", "", 0, false)
        }
    }

    @ApiOperation(httpMethod = "PUT", value = "确认收货")
    @RequestMapping(value = "/orders/{order_id}/received", method = arrayOf(RequestMethod.PUT))
    fun receivedOneOrder(@PathVariable order_id: Long,
                         response: HttpServletResponse): Order {
        try {
            //验证订单当前状态,只有unrecv状态可以确认收货,状态置为unrate
            val order = orderService.findAll(mapOf("order_id" to order_id)).first()

            if (!order.type.isNullOrEmpty()) {
                //记录收货时间
                val map = mapOf("orderId" to order_id, "type" to OrderType.UNRATE.type, "receivedTime" to Date().time)
                orderService.changeOrderType(map)
                LOGGER.info("---确认收货,记录收货时间:" + JSON.toJSONString(map))
                order.type = OrderType.UNRATE.type
            } else {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("当前状态不可确认收货", "UTF-8"))
                return order
            }

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            return order
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return Order(0, "", 0, 0, 0, 0, "", 0, 0, 0, null, null, null, 0, "", "", 0, false)
        }
    }

    @ApiOperation(httpMethod = "DELETE", value = "删除一个订单")
    @RequestMapping(value = "/orders/{order_id}", method = arrayOf(RequestMethod.DELETE))
    fun deleteOneOrder(@PathVariable order_id: Long,
                         response: HttpServletResponse) : Map<Any,Any> {
        try {
            //验证订单当前状态,只有unrate,rated,dealclose状态可以删除,状态置为deleted
            var order = orderService.findAll(mapOf("order_id" to order_id)).first()
            if (!order.type.isNullOrEmpty() && ( order.type.equals(OrderType.DEALCLOSE.type) || order.type.equals(OrderType.UNRATE.type) || order.type.equals(OrderType.RATED.type) )) {
                orderService.changeOrderType(mapOf("orderId" to order_id, "type" to OrderType.DELETED.type))
            } else {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("当前状态不可删除", "UTF-8"))
                return mapOf()
            }

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


    @ApiOperation(httpMethod = "GET", value = "获取订单物流信息", response = Logistics::class)
    @RequestMapping(value = "/orders/{order_id}/logistics", method = arrayOf(RequestMethod.GET))
    fun getLogisticsByOrder(
            @PathVariable order_id: Long,
            response: HttpServletResponse
    ): JSONObject? {

        try {
//            val address1 = Address(1, "收货人1", "北京", "朝阳", "", "详细地址1", "13811111111", true, Date().time, 1)
//
//            val goods2 = Goods(2, 1, 1, 1, "测试商品2", "商品介绍商品介绍商品介绍商品介绍商品介绍",
//                    "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                    "https://www.baidu.com", "1", "1", 1, 32, 1, 2, 123321, 35, 200, null, true,"")
//            val order4 = Order(3, "订单编号4", 1234556, 1, 1, 1457494808497, OrderType.UNRECV.type, 100000, 5000, 105000, address1, listOf(OrderItem(1, 1, 1, goods2, 1, 1, "商品名称", 100, 1457494808497, "", true)), "商品1,商品2等 2 件商品", 1457494808497, "", "", 0, false)
//
//            val step1 = Step("2016-03-08 15:33:30", "已取件,到达[XXX]")
//            val step2 = Step("2016-03-08 16:33:30", "离开 [XXX] 发往 [XXX]")
//            val step3 = Step("2016-03-08 17:33:30", "到达 [XXX]")
//
//            val logistics = Logistics("宅急送", "3847109081", "物流公司已取件", "2016-03-09 18:33:30", null, null, null, arrayOf(step1, step2, step3), order4)

            //查询订单快递单号
            val shippingCode = orderService.getShippingCode(order_id)
            if (shippingCode.isEmpty() || shippingCode == null) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("无物流信息", "UTF-8"))
                return null
            }

            //快递100 ID
            val kuaidiId = UrlEnum.KUAIDI100KEY.url
            val com = "zhaijisong"
            //返回类型：
//            0：返回json字符串，
//            1：返回xml对象，
//            2：返回html对象，
//            3：返回text文本。
//            如果不填，默认返回json字符串。
            val show = "0"
//            返回信息数量：
//            1:返回多行完整的信息，
//            0:只返回一行信息。
//            不填默认返回多行。
            val muti = "1"
//            排序：
//            desc：按时间由新到旧排列，
//            asc：按时间由旧到新排列。
//            不填默认返回倒序（大小写不敏感）
            val order = "desc"
            val kuaidiUrl = "http://api.kuaidi100.com/api?id=$kuaidiId&com=$com&nu=$shippingCode&show=$show&muti=$muti&order=$order"
            LOGGER.info(kuaidiUrl)
            var result = Requests.get(kuaidiUrl).verify(false).text()
            LOGGER.info("-------查询物流信息结果:${result.body}")

//            val str = """{"message":"ok","status":"1","state":"3","data":[{"time":"2012-07-07 13:35:14","context":"客户已签收"},{"time":"2012-07-07 09:10:10","context":"离开 [北京石景山营业厅] 派送中，递送员[温]，电话[]"},{"time":"2012-07-06 19:46:38","context":"到达 [北京石景山营业厅]"},{"time":"2012-07-06 15:22:32","context":"离开 [北京石景山营业厅] 派送中，递送员[温]，电话[]"},{"time":"2012-07-06 15:05:00","context":"到达 [北京石景山营业厅]"},{"time":"2012-07-06 13:37:52","context":"离开 [北京_同城中转站] 发往 [北京石景山营业厅]"},{"time":"2012-07-06 12:54:41","context":"到达 [北京_同城中转站]"},{"time":"2012-07-06 11:11:03","context":"离开 [北京运转中心驻站班组] 发往 [北京_同城中转站]"},{"time":"2012-07-06 10:43:21","context":"到达 [北京运转中心驻站班组]"},{"time":"2012-07-05 21:18:53","context":"离开 [福建_厦门支公司] 发往 [北京运转中心_航空]"},{"time":"2012-07-05 20:07:27","context":"已取件，到达 [福建_厦门支公司]"}]}"""

//            var kuaidiInfo = JSON.parseObject(str, KuaidiInfo::class.java)
//            kuaidiInfo.dataSource = "宅急送"
//            kuaidiInfo.shippingCode = shippingCode

            var jsonObject = JSON.parseObject(result.body)

            if (jsonObject.isEmpty() || jsonObject == null) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("无物流信息", "UTF-8"))
                return null
            }

            val state = jsonObject.getString("state")
            val orderStatus = if (state.equals("0")) "运送中"
            else if (state.equals("1")) "快递公司已揽件"
            else if (state.equals("2")) "疑难件"
            else if (state.equals("3")) "已签收"
            else if (state.equals("4")) "已退签"
            else if (state.equals("5")) "正在同城派件"
            else if (state.equals("6")) "正在退回"
            else "状态错误"

            jsonObject.putAll(arrayOf("dataSource" to "宅急送", "shippingCode" to shippingCode, "orderStatus" to orderStatus))

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return jsonObject
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return null
        }

    }

    @ApiOperation(httpMethod = "POST", value = "添加订单商品评论")
    @RequestMapping(value = "/orders/{order_id}/comment", method = arrayOf(RequestMethod.POST))
    fun addOrderComment(@PathVariable(value = "order_id") order_id: Long,
                        @RequestBody(required = true) commentForm: CommentForm,
                        response: HttpServletResponse): Comment {
        try {

            LOGGER.info("----------请求参数:${JSON.toJSON(commentForm)}")

            var userId = commentForm.userId
            var goodsId = commentForm.goodsId
            var rate = commentForm.rate
            var content = commentForm.content

            if (goodsId.equals(null) || goodsId.equals(0L) || userId.equals(null) || userId.equals(0L) ||
                    rate.equals(null) || rate.equals(0) || content.equals(null) || content.equals("")) {
                LOGGER.info("-----------请求参数错误:${JSON.toJSON(commentForm)}")
                response.status = HttpServletResponse.SC_BAD_REQUEST
                response.addHeader("X-Err-Message", URLEncoder.encode("请求参数错误", "UTF-8"))
                return Comment(0, 0, 0, 0, 0, 0, "", 0, 0, "", "", "", null, 0)
            }

            val date = Date().time
            val skuId = commentForm.skuId
            var comment = Comment(0, commentForm.userId, order_id, commentForm.goodsId, skuId, commentForm.rate, commentForm.content, commentForm.purchaseTime, date, commentForm.userImageUrl, commentForm.userNickName, null, commentForm.images, date)

            //根据skuid获取sku属性,序列化为json,赋给comment

            var skuAttr = orderService.getSkuAttrs(skuId)

            var skuAttrJson = JSON.toJSONString(skuAttr)

            comment.skuProperty = skuAttrJson

            commentService.save(comment)

            //存入评论图片集
            var images = commentForm.images
            var imageIds = emptyList<Long>()
            if (images != null && images.isNotEmpty()) {
                for (image in images) {
                    //                image.createTime = Date().time
                    imageService.save(image)
                    imageIds = imageIds.plus(image.imgId)
                }
                //存入评论图片关联表
                var commentRefImage = mapOf("commentId" to comment.commentId, "imageIds" to imageIds)
                imageService.saveCommentRefImage(commentRefImage)
            }

            //将此orderitem状态置为 rated
            orderService.changeOrderItemType(mapOf("orderId" to order_id, "skuId" to skuId, "itemType" to OrderItemType.RATED.type));

            //查询此订单下的商品是否都已评价,如果都评价,则订单状态更新为rated
            var itemTypes: List<String?>? = orderService.getItemTypes(order_id)
            var allRated = false
            if (itemTypes != null && itemTypes.isNotEmpty()) {
                //为空或未评论过
                var unratedCount = itemTypes.filter { if (it.isNullOrEmpty()) true else !it.equals(OrderItemType.RATED.type) }.count()
                if (unratedCount == 0) {
                    allRated = true
                }
            }
            if (allRated) {
                orderService.changeOrderType(mapOf("orderId" to order_id, "type" to OrderType.RATED.type))
            }

            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            return comment
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return Comment(0, 0, 0, 0, 0, 0, "", 0, 0, "", "", "", null, 0)
        }
    }

}