package com.nsg.controller

import com.nsg.domain.Shop
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

/**
 * Created by lijc on 16/2/20.
 */

@Api(basePath = "/v1/mall/csl", value = "Store", description = "商城API", produces = "application/json", position = 1)
@RestController
@RequestMapping("/v1/mall/csl")
class ShopController {


    @ApiOperation(httpMethod = "GET", value = "商城首页", response = Map::class)
    @RequestMapping(value = "/store", method = arrayOf(RequestMethod.GET))
    fun getStore(response: HttpServletResponse) : Shop {
        response.status = HttpServletResponse.SC_OK
        return Shop(1,"csl","中超")
    }


}