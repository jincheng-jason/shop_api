package com.nsg.controller

import com.alibaba.fastjson.JSON
import com.nsg.domain.PropsWasteType
import com.nsg.domain.data.PropsData
import com.nsg.domain.form.PropsPrizeFrom
import com.nsg.domain.form.ShopIds
import com.nsg.service.OrderService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder

/**
 * Created by lijc on 16/3/8.
 */

@Api(basePath = "/v1/mall/csl", value = "props", description = "虚拟道具API", produces = "application/json", position = 9)
@RestController
@RequestMapping("/v1/mall/csl")
class PropsController
@Autowired constructor(var orderService: OrderService) {

    private val LOGGER = LoggerFactory.getLogger(PropsController::class.java)

    @ApiOperation(httpMethod = "GET", value = "获取虚拟道具列表", response = Array<PropsData>::class)
    @RequestMapping(value = "/props", method = arrayOf(RequestMethod.GET))
    fun getProps(
    ): Array<PropsData> {

        return arrayOf(PropsData(1), PropsData(2), PropsData(3))
    }


    @ApiOperation(httpMethod = "POST", value = "绑定用户中奖虚拟卡", response = Array<PropsData>::class)
    @RequestMapping(value = "/props_prize", method = arrayOf(RequestMethod.POST))
    fun bindPropsPrize(
            @RequestBody(required = true) propsPrizeFrom: PropsPrizeFrom
    ): Map<String, Any> {

        try {

            LOGGER.info("绑定用户中奖虚拟卡:${JSON.toJSONString(propsPrizeFrom)}")

            //首先,查询流水单表中是否有足够的中奖流水
            val itemKey = propsPrizeFrom.itemKey
            val type = PropsWasteType.PRIZE.type
            val buyNum = propsPrizeFrom.count
            val nhId = propsPrizeFrom.nhId
            val nickName = URLDecoder.decode(propsPrizeFrom.userNickName, "UTF-8")
            val shopCode = propsPrizeFrom.shopCode
            val shopId = ShopIds.valueOf(shopCode.toUpperCase()).shopId

            val count = orderService.getPropsWasteCount(mapOf("itemKey" to itemKey, "type" to type, "shopId" to shopId))
            if (buyNum > count) {
                //库存不足
                LOGGER.info("绑定用户中奖虚拟卡库存不足:buyNum=${buyNum},count=${count}")
                return mapOf("oper_code" to 0, "result" to "Inventory shortage")
            }

            //绑定用户虚拟卡,并消耗中奖流水
            if (orderService.bindUserItems(null, null, buyNum, type, "用户中奖绑定虚拟物品", shopId, itemKey, nhId, nickName)) {
                LOGGER.info("绑定用户中奖虚拟卡成功${JSON.toJSONString(propsPrizeFrom)}")
                return mapOf("oper_code" to 1, "result" to "Bind success")
            } else {
                LOGGER.info("绑定用户中奖虚拟卡失败${JSON.toJSONString(propsPrizeFrom)}")
                return mapOf("oper_code" to 0, "result" to "Bind failed")
            }


        } catch(e: Exception) {
            e.printStackTrace()
            LOGGER.info("绑定用户中奖虚拟卡失败${JSON.toJSONString(propsPrizeFrom)}")
            return mapOf("oper_code" to 0, "result" to "Bind failed")
        }

    }


}