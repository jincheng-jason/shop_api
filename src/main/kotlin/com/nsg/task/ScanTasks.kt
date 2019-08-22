package com.nsg.task

import com.nsg.domain.Comment
import com.nsg.domain.OrderType
import com.nsg.domain.form.ShopIds
import com.nsg.service.CommentService
import com.nsg.service.GoodsService
import com.nsg.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Created by lijc on 16/4/6.
 */

@Component
class ScanTasks
@Autowired constructor(
        val orderService: OrderService,
        val goodsService: GoodsService,
        val commentService: CommentService) {

    private val LOGGER = LoggerFactory.getLogger(ScanTasks::class.java)


    //扫描待确认订单并查询支付中心,如果订单未支付,则将订单状态置为未支付
//    @Scheduled(fixedDelay = 20000)
//    fun scanCheckingOrders() {
//        val checkingOrders = orderService.getOrdersByType(OrderType.CHECKING.type)
//        for (order in checkingOrders) {
//            orderService.getPayStatus(order.orderId, order.payType)
//        }
//    }

    //扫描未支付订单,获取其过期时间,如果过期时间小于当前时间,则订单状态置为关闭,并释放库存
    @Scheduled(fixedDelay = 30000)
    fun scanUnpayOrders() {

        val unpayOrders = orderService.getOrdersByType(OrderType.UNPAY.type)
        for (order in unpayOrders) {
            if (order.overdueTime <= Date().time) {
                orderService.changeOrderType(mapOf("orderId" to order.orderId, "type" to OrderType.DEALCLOSE.type))
            }

            //扫描未支付订单,当时间到达失效时间前10分钟时,调用系统通知接口
            //order表中新增字段 unpay_notify_status 未支付提醒状态,发送通知后置为true
            val now = LocalDateTime.now()
            val zoneId = ZoneId.systemDefault()
            val nowTime = now.atZone(zoneId).toInstant().toEpochMilli()
            val messageTime: Long = Instant.ofEpochMilli(order.overdueTime).atZone(zoneId).minusMinutes(10).toInstant().toEpochMilli()
            val content = "您的未支付订单【${order.description}】将于10分钟后自动取消，点击完成支付>>"
            if (nowTime >= messageTime && nowTime < order.overdueTime && !order.overdueMessageStatus) {
                if (order.shopId == ShopIds.CSL.shopId) {
                    var messageMap = mapOf(
                            "isAll" to 0,
                            "type" to "common",
                            "toUserId" to order.userId,
                            "content" to content,
                            "action" to "csl://orders",
                            "orderId" to order.orderId
                    )
                    orderService.sendSystemMessage(messageMap)
                }

            }
        }

    }

    //扫描goods表,获取每件商品的sku最高最低价,并更新
//    @Scheduled(cron = "0 0 2 * * ?")
//            //    @Scheduled(cron = "0/10 * * * * ? ")
//    fun scanGoodsPrice() {
//        LOGGER.info("扫描goods表,获取每件商品的sku最高最低价,并更新")
//        val goodsList = goodsService.getCommendGoodsList(mapOf())
//        for (goods in goodsList) {
//            goodsService.getPriceSummaryStatistics(goods.goodsId)
//        }
//    }

    //扫描未评价订单,到自动评价时限后,自动评为5星
    @Scheduled(fixedDelay = 60000)
    fun scanUnrateOrders() {
        val unrateOrders = orderService.getOrdersByType(OrderType.UNRATE.type)
        for (order in unrateOrders) {
            val now = LocalDateTime.now()
            val zoneId = ZoneId.systemDefault()
            val nowTime = now.atZone(zoneId).toInstant().toEpochMilli()
            //            val autoRateTime: Long = Instant.ofEpochMilli(order.receivedTime).atZone(zoneId).plusMinutes(10).toInstant().toEpochMilli()
            val autoRateTime: Long = Instant.ofEpochMilli(order.receivedTime).atZone(zoneId).plusDays(7).toInstant().toEpochMilli()
            val content = "评价方未及时做出评价，系统默认好评!"

            if (autoRateTime <= nowTime) {

                LOGGER.info(content + "---" + order.orderId)
                val orderItems = orderService.getOrderItemsByOrderId(order.orderId)
                for (orderItem in orderItems) {
                    //                    val userUrl = UrlEnum.USER_INFO_URL.url + order.userId
                    //                    LOGGER.info(userUrl)
                    //                    val userResponse = Requests.get(userUrl).verify(false).text()
                    //                    LOGGER.info("自动评价:获取用户uuid:${userResponse.body}")
                    //                    val userMap = JSON.parse(userResponse.body) as Map<*, *>
                    val userMap = orderService.getUserInfo(order.userId)
                    val data = (userMap["data"] ?: emptyMap<Any, Any>()) as Map<*, *>
                    //                    val uuid = data["nhId"]
                    val userImageUrl = (data["avatar"] ?: "") as String
                    val nickName = (data["nickName"] ?: "") as String
                    val comment = Comment(0, order.userId, order.orderId, orderItem.goodsId, orderItem.skuId, 5, content, order.createTime, nowTime, userImageUrl, nickName, "[]", null, nowTime)
                    commentService.save(comment)
                }
                orderService.changeOrderType(mapOf("orderId" to order.orderId, "type" to OrderType.RATED.type))
                LOGGER.info("自动评价完成:${order.orderId}")
            }

        }
    }


}